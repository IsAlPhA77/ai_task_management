package com.scu.ai_task_management.task_priority_recommendation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationDTO;
import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationVO;
import com.scu.ai_task_management.task_priority_recommendation.service.TaskPriorityRecommendationService;
import com.scu.ai_task_management.task_query.domain.TaskHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务优先级推荐服务实现
 */
@Slf4j
@Service
public class TaskPriorityRecommendationServiceImpl implements TaskPriorityRecommendationService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;


    @Override
    public List<PriorityRecommendationVO> getPriorityRecommendations(Long userId, PriorityRecommendationDTO requestDTO) {
        log.info("获取任务优先级推荐: userId={}, request={}", userId, requestDTO);

        // 获取待分析的任务
        List<Task> tasks = getTasksForAnalysis(userId, requestDTO);

        // 计算优先级推荐
        List<PriorityRecommendationVO> recommendations = tasks.stream()
                .map(task -> calculatePriorityRecommendation(task))
                .filter(Objects::nonNull)
                .sorted((a, b) -> Integer.compare(b.getRecommendedPriority(), a.getRecommendedPriority()))
                .collect(Collectors.toList());

        log.info("优先级推荐计算完成，共{}个任务", recommendations.size());
        return recommendations;
    }

    @Override
    @Transactional
    public void applyPriorityRecommendation(Long userId, Long taskId, Integer recommendedPriority) {
        log.info("应用优先级推荐: userId={}, taskId={}, priority={}", userId, taskId, recommendedPriority);

        Task task = taskUtil.getOwnedTask(userId, taskId);
        Integer oldPriority = task.getPriority();

        // 更新优先级
        task.setPriority(recommendedPriority);
        taskMapper.updateById(task);

        // 记录历史
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("priority", oldPriority);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("priority", recommendedPriority);

        taskUtil.saveTaskHistory(taskId, userId, TaskHistory.ActionType.UPDATE,
                oldValue, newValue, "应用优先级推荐");

        log.info("优先级推荐应用成功: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void batchApplyPriorityRecommendations(Long userId, List<PriorityRecommendationVO> recommendations) {
        log.info("批量应用优先级推荐: userId={}, count={}", userId, recommendations.size());

        for (PriorityRecommendationVO recommendation : recommendations) {
            try {
                applyPriorityRecommendation(userId, recommendation.getTaskId(), recommendation.getRecommendedPriority());
            } catch (Exception e) {
                log.error("应用优先级推荐失败: taskId={}, error={}", recommendation.getTaskId(), e.getMessage());
            }
        }

        log.info("批量应用优先级推荐完成");
    }

    @Override
    @Transactional
    public void recalculateAllTaskPriorities(Long userId) {
        log.info("重新计算所有任务优先级: userId={}", userId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
                .eq(Task::getIsDeleted, false);

        List<Task> tasks = taskMapper.selectList(wrapper);

        for (Task task : tasks) {
            try {
                PriorityRecommendationVO recommendation = calculatePriorityRecommendation(task);
                if (recommendation != null && !recommendation.getRecommendedPriority().equals(task.getPriority())) {
                    task.setPriority(recommendation.getRecommendedPriority());
                    taskMapper.updateById(task);
                }
            } catch (Exception e) {
                log.error("重新计算任务优先级失败: taskId={}, error={}", task.getId(), e.getMessage());
            }
        }

        log.info("重新计算所有任务优先级完成");
    }

    /**
     * 获取需要分析的任务
     */
    private List<Task> getTasksForAnalysis(Long userId, PriorityRecommendationDTO requestDTO) {
        taskUtil.ensureUserId(userId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
                .eq(Task::getIsDeleted, false);

        // 按状态过滤
        if (!requestDTO.getIncludeCompleted()) {
            wrapper.ne(Task::getStatus, Task.TaskStatus.COMPLETED);
        }
        if (!requestDTO.getIncludeCancelled()) {
            wrapper.ne(Task::getStatus, Task.TaskStatus.CANCELLED);
        }

        // 指定任务ID过滤
        if (requestDTO.getTaskIds() != null && !requestDTO.getTaskIds().isEmpty()) {
            wrapper.in(Task::getId, requestDTO.getTaskIds());
        }

        return taskMapper.selectList(wrapper);
    }


    /**
     * 计算单个任务的优先级推荐
     */
    private PriorityRecommendationVO calculatePriorityRecommendation(Task task) {
        try {
            int basePriority = 0;
            List<String> reasons = new ArrayList<>();
            boolean isUrgent = false;

            // 1. 截止时间因素
            String deadlinePressure = calculateDeadlinePressure(task);
            if (deadlinePressure != null) {
                int deadlineScore = getDeadlineScore(task.getDeadline());
                basePriority += deadlineScore;
                reasons.add("截止时间: " + deadlinePressure);
                if (deadlineScore >= 30) {
                    isUrgent = true;
                }
            }

            // 2. 依赖关系因素
            String dependencyImpact = calculateDependencyImpact(task);
            int dependencyScore = getDependencyScore(task);
            basePriority += dependencyScore;
            reasons.add("依赖关系: " + dependencyImpact);

            // 3. 任务类型因素
            String categoryImpact = calculateCategoryImpact(task);
            if (categoryImpact != null) {
                int categoryScore = getCategoryScore(task.getCategory());
                basePriority += categoryScore;
                reasons.add("任务类型: " + categoryImpact);
            }

            // 4. 任务状态因素
            int statusScore = getStatusScore(task.getStatus());
            if (statusScore > 0) {
                basePriority += statusScore;
                reasons.add("任务状态: " + getStatusImpact(task.getStatus()));
            }

            // 5. 预估耗时因素
            if (task.getEstimatedDuration() != null) {
                int durationScore = getDurationScore(task.getEstimatedDuration());
                basePriority += durationScore;
                reasons.add("预估耗时: " + getDurationImpact(task.getEstimatedDuration()));
            }

            // 确保优先级在合理范围内
            int recommendedPriority = Math.max(0, Math.min(100, basePriority));
            int priorityChange = recommendedPriority - (task.getPriority() != null ? task.getPriority() : 0);

            return PriorityRecommendationVO.builder()
                    .taskId(task.getId())
                    .taskTitle(task.getTitle())
                    .currentPriority(task.getPriority() != null ? task.getPriority() : 0)
                    .recommendedPriority(recommendedPriority)
                    .priorityChange(priorityChange)
                    .reasons(reasons)
                    .urgent(isUrgent)
                    .deadlinePressure(deadlinePressure)
                    .dependencyImpact(dependencyImpact)
                    .categoryImpact(categoryImpact)
                    .build();

        } catch (Exception e) {
            log.error("计算任务优先级推荐失败: taskId={}, error={}", task.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 计算截止时间压力
     */
    private String calculateDeadlinePressure(Task task) {
        if (task.getDeadline() == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilDeadline = ChronoUnit.HOURS.between(now, task.getDeadline());

        if (hoursUntilDeadline < 0) {
            return "已过期";
        } else if (hoursUntilDeadline <= 24) {
            return "24小时内到期";
        } else if (hoursUntilDeadline <= 72) {
            return "3天内到期";
        } else if (hoursUntilDeadline <= 168) {
            return "1周内到期";
        } else {
            return "长期任务";
        }
    }

    /**
     * 计算依赖关系影响
     */
    private String calculateDependencyImpact(Task task) {
        if (task.getDependencies() == null || task.getDependencies().isEmpty()) {
            return "无依赖";
        }

        // 检查依赖任务的状态
        List<Task> dependencies = taskMapper.selectBatchIds(task.getDependencies());
        long completedCount = dependencies.stream()
                .filter(dep -> dep.getStatus() == Task.TaskStatus.COMPLETED)
                .count();

        if (completedCount == dependencies.size()) {
            return "所有依赖已完成";
        } else if (completedCount == 0) {
            return "所有依赖未完成";
        } else {
            return "部分依赖已完成";
        }
    }

    /**
     * 计算任务类型影响
     */
    private String calculateCategoryImpact(Task task) {
        if (task.getCategory() == null) {
            return null;
        }

        String category = task.getCategory().toLowerCase();
        if (category.contains("紧急") || category.contains("urgent")) {
            return "紧急类型";
        } else if (category.contains("重要") || category.contains("important")) {
            return "重要类型";
        } else if (category.contains("会议") || category.contains("meeting")) {
            return "会议类型";
        } else {
            return "普通类型";
        }
    }

    /**
     * 获取截止时间分数
     */
    private int getDeadlineScore(LocalDateTime deadline) {
        if (deadline == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilDeadline = ChronoUnit.HOURS.between(now, deadline);

        if (hoursUntilDeadline < 0) {
            return 50; // 已过期，高优先级
        } else if (hoursUntilDeadline <= 1) {
            return 40; // 1小时内
        } else if (hoursUntilDeadline <= 6) {
            return 35; // 6小时内
        } else if (hoursUntilDeadline <= 24) {
            return 30; // 24小时内
        } else if (hoursUntilDeadline <= 72) {
            return 20; // 3天内
        } else if (hoursUntilDeadline <= 168) {
            return 10; // 1周内
        } else {
            return 0; // 长期
        }
    }

    /**
     * 获取依赖关系分数
     */
    private int getDependencyScore(Task task) {
        if (task.getDependencies() == null || task.getDependencies().isEmpty()) {
            return 0;
        }

        List<Task> dependencies = taskMapper.selectBatchIds(task.getDependencies());
        long blockedCount = dependencies.stream()
                .filter(dep -> dep.getStatus() != Task.TaskStatus.COMPLETED)
                .count();

        // 被阻塞的任务优先级更高
        return (int) (blockedCount * 5);
    }

    /**
     * 获取任务类型分数
     */
    private int getCategoryScore(String category) {
        if (category == null) {
            return 0;
        }

        String cat = category.toLowerCase();
        if (cat.contains("紧急") || cat.contains("urgent")) {
            return 25;
        } else if (cat.contains("重要") || cat.contains("important")) {
            return 15;
        } else if (cat.contains("会议") || cat.contains("meeting")) {
            return 10;
        } else {
            return 0;
        }
    }

    /**
     * 获取任务状态分数
     */
    private int getStatusScore(Task.TaskStatus status) {
        if (status == null) {
            return 0;
        }

        switch (status) {
            case IN_PROGRESS:
                return 10; // 进行中的任务优先级稍高
            case TODO:
                return 5;  // 待办任务基础优先级
            default:
                return 0;
        }
    }

    /**
     * 获取状态影响描述
     */
    private String getStatusImpact(Task.TaskStatus status) {
        if (status == null) {
            return "未开始";
        }

        switch (status) {
            case IN_PROGRESS:
                return "正在进行";
            case TODO:
                return "待开始";
            case COMPLETED:
                return "已完成";
            case CANCELLED:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取耗时分数
     */
    private int getDurationScore(Integer estimatedDuration) {
        if (estimatedDuration == null) {
            return 0;
        }

        // 耗时越短的任务优先级相对较高
        if (estimatedDuration <= 30) {
            return 5;  // 30分钟以内
        } else if (estimatedDuration <= 60) {
            return 3;  // 1小时以内
        } else if (estimatedDuration <= 240) {
            return 1;  // 4小时以内
        } else {
            return 0;  // 超过4小时
        }
    }

    /**
     * 获取耗时影响描述
     */
    private String getDurationImpact(Integer estimatedDuration) {
        if (estimatedDuration == null) {
            return "未预估";
        }

        if (estimatedDuration <= 30) {
            return "短任务";
        } else if (estimatedDuration <= 60) {
            return "中等任务";
        } else if (estimatedDuration <= 240) {
            return "较长任务";
        } else {
            return "长任务";
        }
    }
}
