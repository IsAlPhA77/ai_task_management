package com.scu.ai_task_management.task_statistic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_statistic.model.TaskStatisticsVO;
import com.scu.ai_task_management.task_statistic.service.TaskStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TaskStatisticServiceImpl implements TaskStatisticService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;

    @Override
    public TaskStatisticsVO getTaskStatistics(Long userId) {
        log.info("获取任务统计信息，userId={}", userId);
        taskUtil.ensureUserId(userId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId);
        List<Task> allTasks = taskMapper.selectList(wrapper);

        long totalTasks = allTasks.size();
        long todoTasks = allTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.TODO)
                .count();
        long inProgressTasks = allTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .count();
        long completedTasks = allTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        long cancelledTasks = allTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.CANCELLED)
                .count();

        // 查询逾期任务
        List<Task> overdueTasks = taskMapper.findOverdueTasks(userId, LocalDateTime.now());
        long overdueTaskCount = overdueTasks.size();

        // 计算完成率
        BigDecimal completionRate = BigDecimal.ZERO;
        if (totalTasks > 0) {
            completionRate = BigDecimal.valueOf(completedTasks)
                    .divide(BigDecimal.valueOf(totalTasks), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 计算平均实际耗时
        double avgActualDuration = allTasks.stream()
                .filter(t -> t.getActualDuration() != null)
                .mapToInt(Task::getActualDuration)
                .average()
                .orElse(0.0);

        return TaskStatisticsVO.builder()
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTaskCount)
                .completionRate(completionRate)
                .avgActualDuration(avgActualDuration)
                .build();
    }

}
