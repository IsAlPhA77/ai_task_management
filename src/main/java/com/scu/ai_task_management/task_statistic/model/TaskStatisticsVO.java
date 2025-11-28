package com.scu.ai_task_management.task_statistic.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 任务统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务统计信息")
public class TaskStatisticsVO {

    @Schema(description = "总任务数", example = "100")
    private Long totalTasks;

    @Schema(description = "待办任务数", example = "30")
    private Long todoTasks;

    @Schema(description = "进行中任务数", example = "20")
    private Long inProgressTasks;

    @Schema(description = "已完成任务数", example = "45")
    private Long completedTasks;

    @Schema(description = "已取消任务数", example = "5")
    private Long cancelledTasks;

    @Schema(description = "逾期任务数", example = "8")
    private Long overdueTasks;

    @Schema(description = "完成率（%）", example = "45.00")
    private BigDecimal completionRate;

    @Schema(description = "平均实际耗时（分钟）", example = "135")
    private Double avgActualDuration;
}
