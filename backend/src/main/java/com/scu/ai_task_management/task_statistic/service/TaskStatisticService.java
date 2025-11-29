package com.scu.ai_task_management.task_statistic.service;

import com.scu.ai_task_management.task_statistic.model.TaskStatisticsVO;

/**
 * 任务统计服务接口
 */
public interface TaskStatisticService {

    TaskStatisticsVO getTaskStatistics(Long userId);

}
