package com.scu.ai_task_management.task_statistic.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_statistic.model.TaskStatisticsVO;
import com.scu.ai_task_management.task_statistic.service.TaskStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务统计", description = "任务的统计接口")
public class TaskStatisticController {

    @Autowired
    private TaskStatisticService taskStatisticService;

    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取任务的统计信息（总数、完成率等）")
    public Result<TaskStatisticsVO> getStatistics(@CurrentUserId Long userId) {
        log.info("接收获取任务统计请求");
        TaskStatisticsVO statistics = taskStatisticService.getTaskStatistics(userId);
        return Result.success(statistics);
    }

}
