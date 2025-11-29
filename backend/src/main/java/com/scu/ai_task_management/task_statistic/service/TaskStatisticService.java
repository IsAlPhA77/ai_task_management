package com.scu.ai_task_management.task_statistic.service;

import com.scu.ai_task_management.task_statistic.model.TaskStatisticsVO;

public interface TaskStatisticService {

    TaskStatisticsVO getTaskStatistics(Long userId);

}
