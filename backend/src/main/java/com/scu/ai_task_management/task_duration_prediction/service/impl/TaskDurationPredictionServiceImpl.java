package com.scu.ai_task_management.task_duration_prediction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.infra.ai.model.TaskParseRequest;
import com.scu.ai_task_management.infra.ai.service.AIService;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionRequestDTO;
import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionVO;
import com.scu.ai_task_management.task_duration_prediction.service.TaskDurationPredictionService;
import com.scu.ai_task_management.task_query.domain.TaskHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务耗时预测服务实现
 */
@Slf4j
@Service
public class TaskDurationPredictionServiceImpl implements TaskDurationPredictionService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;

    @Autowired
    private AIService aiService;

    // 简单缓存，避免重复计算
    private final Map<String, DurationPredictionVO> predictionCache = new HashMap<>();

    @Override
    public List<DurationPredictionVO> getDurationPredictions(Long userId, DurationPredictionRequestDTO requestDTO) {
        log.info("获取任务耗时预测: userId={}, request={}", userId, requestDTO);

        // 获取待预测的任务
        List<Task> tasks = getTasksForPrediction(userId, requestDTO);

        // 生成预测结果
        List<DurationPredictionVO> predictions = tasks.stream()
                .map(task -> getDurationPrediction(userId, task, requestDTO))
                .filter(Objects::nonNull)
                .filter(prediction -> prediction.getConfidence() >= requestDTO.getMinConfidence())
                .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
                .collect(Collectors.toList());

        log.info("耗时预测完成，共{}个任务", predictions.size());
        return predictions;
    }

    @Override
    public DurationPredictionVO predictTaskDuration(Long userId, Long taskId) {
        log.info("预测单个任务耗时: userId={}, taskId={}", userId, taskId);

        Task task = taskUtil.getOwnedTask(userId, taskId);
        DurationPredictionRequestDTO requestDTO = DurationPredictionRequestDTO.builder()
                .predictionMethod(DurationPredictionRequestDTO.PredictionMethod.HYBRID)
                .build();

        return getDurationPrediction(userId, task, requestDTO);
    }

    @Override
    @Transactional
    public void applyDurationPrediction(Long userId, Long taskId, Integer predictedDuration) {
        log.info("应用耗时预测: userId={}, taskId={}, duration={}", userId, taskId, predictedDuration);

        Task task = taskUtil.getOwnedTask(userId, taskId);
        Integer oldDuration = task.getEstimatedDuration();

        // 更新预估耗时
        task.setEstimatedDuration(predictedDuration);
        taskMapper.updateById(task);

        // 记录历史
        Map<String, Object> oldValue = new HashMap<>();
        oldValue.put("estimatedDuration", oldDuration);

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("estimatedDuration", predictedDuration);

        taskUtil.saveTaskHistory(taskId, userId, TaskHistory.ActionType.UPDATE,
                oldValue, newValue, "应用耗时预测");

        log.info("耗时预测应用成功: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void batchApplyDurationPredictions(Long userId, List<DurationPredictionVO> predictions) {
        log.info("批量应用耗时预测: userId={}, count={}", userId, predictions.size());

        for (DurationPredictionVO prediction : predictions) {
            try {
                applyDurationPrediction(userId, prediction.getTaskId(), prediction.getPredictedDuration());
            } catch (Exception e) {
                log.error("应用耗时预测失败: taskId={}, error={}", prediction.getTaskId(), e.getMessage());
            }
        }

        log.info("批量应用耗时预测完成");
    }

    // TODO
    @Override
    public void trainDurationPredictionModel(Long userId) {
        log.info("训练耗时预测模型: userId={}", userId);

        predictionCache.clear();

        log.info("耗时预测模型训练完成（暂未实现实际训练逻辑）");
    }

    /**
     * 获取需要预测的任务
     */
    private List<Task> getTasksForPrediction(Long userId, DurationPredictionRequestDTO requestDTO) {
        taskUtil.ensureUserId(userId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
                .eq(Task::getIsDeleted, false)
                .ne(Task::getStatus, Task.TaskStatus.COMPLETED);

        if (requestDTO.getTaskIds() != null && !requestDTO.getTaskIds().isEmpty()) {
            wrapper.in(Task::getId, requestDTO.getTaskIds());
        }

        return taskMapper.selectList(wrapper);
    }


    /**
     * 获取任务耗时预测
     */
    private DurationPredictionVO getDurationPrediction(Long userId, Task task, DurationPredictionRequestDTO requestDTO) {
        try {
            String cacheKey = userId + "_" + task.getId() + "_" + requestDTO.getPredictionMethod();
            if (predictionCache.containsKey(cacheKey)) {
                return predictionCache.get(cacheKey);
            }

            DurationPredictionVO prediction = null;

            switch (requestDTO.getPredictionMethod()) {
                case SIMPLE:
                    prediction = predictBySimpleStatistics(userId, task);
                    break;
                case AI:
                    prediction = predictByAI(userId, task);
                    break;
                case HYBRID:
                    prediction = predictByHybridMethod(userId, task);
                    break;
            }

            if (prediction != null) {
                predictionCache.put(cacheKey, prediction);
            }

            return prediction;

        } catch (Exception e) {
            log.error("生成任务耗时预测失败: taskId={}, error={}", task.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 基于简单统计的预测
     */
    private DurationPredictionVO predictBySimpleStatistics(Long userId, Task task) {
        List<String> factors = new ArrayList<>();

        // 1. 基于历史相似任务的平均耗时
        Integer historicalAverage = getHistoricalAverageDuration(userId, task);
        Integer predictedDuration = historicalAverage;
        Double confidence = 0.4; // 基础置信度

        if (historicalAverage != null) {
            factors.add("历史相似任务平均耗时: " + historicalAverage + "分钟");
        } else {
            // 2. 基于任务类型的默认耗时
            predictedDuration = getDefaultDurationByCategory(task.getCategory());
            confidence = 0.2;
            factors.add("基于任务类型的默认耗时: " + predictedDuration + "分钟");
        }

        // 3. 基于描述长度调整
        if (task.getDescription() != null) {
            int descLength = task.getDescription().length();
            if (descLength > 100) {
                predictedDuration = (int) (predictedDuration * 1.2);
                factors.add("任务描述较长，增加20%耗时");
            } else if (descLength < 20) {
                predictedDuration = (int) (predictedDuration * 0.8);
                factors.add("任务描述较短，减少20%耗时");
            }
        }

        // 4. 基于标签数量调整
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            int tagCount = task.getTags().size();
            if (tagCount > 3) {
                predictedDuration = (int) (predictedDuration * 1.1);
                factors.add("标签较多，增加10%耗时");
            }
        }

        return DurationPredictionVO.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .currentEstimatedDuration(task.getEstimatedDuration())
                .predictedDuration(predictedDuration)
                .confidence(confidence)
                .predictionMethod("简单统计")
                .predictionFactors(factors)
                .historicalAverage(historicalAverage)
                .similarTasksCount(getSimilarTasksCount(userId, task))
                .accuracyAssessment(getAccuracyAssessment(confidence))
                .build();
    }

    /**
     * 基于AI的预测
     */
    private DurationPredictionVO predictByAI(Long userId, Task task) {
        try {
            // 构建AI请求
            String prompt = buildDurationPredictionPrompt(task);
            TaskParseRequest request = TaskParseRequest.builder()
                    .naturalLanguage(prompt)
                    .build();

            // 调用AI服务
            var response = aiService.parseNaturalLanguageTask(request);
            if (response.getTasks().isEmpty()) {
                return null;
            }

            var aiTask = response.getTasks().get(0);
            Integer predictedDuration = aiTask.getEstimatedDuration();
            Double confidence = aiTask.getConfidence();

            if (predictedDuration == null) {
                return null;
            }

            List<String> factors = new ArrayList<>();
            factors.add("AI模型分析任务复杂度");
            factors.add("基于自然语言理解的耗时评估");

            return DurationPredictionVO.builder()
                    .taskId(task.getId())
                    .taskTitle(task.getTitle())
                    .currentEstimatedDuration(task.getEstimatedDuration())
                    .predictedDuration(predictedDuration)
                    .confidence(confidence)
                    .predictionMethod("人工智能")
                    .predictionFactors(factors)
                    .historicalAverage(null)
                    .similarTasksCount(0)
                    .accuracyAssessment(getAccuracyAssessment(confidence))
                    .build();

        } catch (Exception e) {
            log.error("AI预测失败，使用简单统计方法: taskId={}", task.getId());
            return predictBySimpleStatistics(userId, task);
        }
    }

    /**
     * 混合预测方法
     */
    private DurationPredictionVO predictByHybridMethod(Long userId, Task task) {
        // 获取简单统计预测
        DurationPredictionVO simplePrediction = predictBySimpleStatistics(userId, task);
        if (simplePrediction == null) {
            return predictByAI(userId, task);
        }

        // 获取AI预测
        DurationPredictionVO aiPrediction = predictByAI(userId, task);

        if (aiPrediction == null) {
            return simplePrediction;
        }

        // 加权平均
        double simpleWeight = 0.4;
        double aiWeight = 0.6;

        int weightedDuration = (int) Math.round(
                simplePrediction.getPredictedDuration() * simpleWeight +
                        aiPrediction.getPredictedDuration() * aiWeight
        );

        double weightedConfidence = Math.max(simplePrediction.getConfidence(), aiPrediction.getConfidence());

        List<String> combinedFactors = new ArrayList<>();
        combinedFactors.addAll(simplePrediction.getPredictionFactors());
        combinedFactors.addAll(aiPrediction.getPredictionFactors());
        combinedFactors.add("混合预测结果");

        return DurationPredictionVO.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .currentEstimatedDuration(task.getEstimatedDuration())
                .predictedDuration(weightedDuration)
                .confidence(weightedConfidence)
                .predictionMethod("混合方法")
                .predictionFactors(combinedFactors)
                .historicalAverage(simplePrediction.getHistoricalAverage())
                .similarTasksCount(simplePrediction.getSimilarTasksCount())
                .accuracyAssessment(getAccuracyAssessment(weightedConfidence))
                .build();
    }

    /**
     * 获取历史平均耗时
     */
    private Integer getHistoricalAverageDuration(Long userId, Task task) {
        try {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getUserId, userId)
                    .eq(Task::getStatus, Task.TaskStatus.COMPLETED)
                    .isNotNull(Task::getActualDuration);

            if (task.getCategory() != null) {
                wrapper.eq(Task::getCategory, task.getCategory());
            }

            List<Task> similarTasks = taskMapper.selectList(wrapper);
            if (similarTasks.isEmpty()) {
                return null;
            }

            return (int) similarTasks.stream()
                    .mapToInt(Task::getActualDuration)
                    .average()
                    .orElse(0);

        } catch (Exception e) {
            log.error("获取历史平均耗时失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 获取相似任务数量
     */
    private Integer getSimilarTasksCount(Long userId, Task task) {
        try {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getUserId, userId)
                    .eq(Task::getStatus, Task.TaskStatus.COMPLETED)
                    .isNotNull(Task::getActualDuration);

            if (task.getCategory() != null) {
                wrapper.eq(Task::getCategory, task.getCategory());
            }

            return Math.toIntExact(taskMapper.selectCount(wrapper));

        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 基于类别的默认耗时
     */
    private Integer getDefaultDurationByCategory(String category) {
        if (category == null) {
            return 60; // 默认1小时
        }

        String cat = category.toLowerCase();
        if (cat.contains("会议") || cat.contains("meeting")) {
            return 90; // 会议默认1.5小时
        } else if (cat.contains("开发") || cat.contains("coding")) {
            return 120; // 开发任务默认2小时
        } else if (cat.contains("测试") || cat.contains("test")) {
            return 60; // 测试任务默认1小时
        } else if (cat.contains("文档") || cat.contains("document")) {
            return 45; // 文档任务默认45分钟
        } else {
            return 60; // 默认1小时
        }
    }

    /**
     * 构建AI预测提示词
     */
    private String buildDurationPredictionPrompt(Task task) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下任务信息，预测完成该任务所需的分钟数：\n\n");
        prompt.append("任务标题: ").append(task.getTitle()).append("\n");

        if (task.getDescription() != null) {
            prompt.append("任务描述: ").append(task.getDescription()).append("\n");
        }

        if (task.getCategory() != null) {
            prompt.append("任务类别: ").append(task.getCategory()).append("\n");
        }

        if (task.getTags() != null && !task.getTags().isEmpty()) {
            prompt.append("标签: ").append(String.join(", ", task.getTags())).append("\n");
        }

        prompt.append("\n请只返回一个整数，表示预测的分钟数。考虑任务的复杂度、所需技能和潜在障碍。");

        return prompt.toString();
    }

    /**
     * 获取准确度评估
     */
    private String getAccuracyAssessment(Double confidence) {
        if (confidence >= 0.8) {
            return "高准确度";
        } else if (confidence >= 0.6) {
            return "中等准确度";
        } else if (confidence >= 0.4) {
            return "低准确度";
        } else {
            return "极低准确度，仅供参考";
        }
    }
}
