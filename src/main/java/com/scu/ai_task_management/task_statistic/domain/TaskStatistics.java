package com.scu.ai_task_management.task_statistic.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务统计实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_statistics")
public class TaskStatistics {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 任务分类
     */
    @TableField("category")
    private String category;

    /**
     * 平均耗时（分钟）
     */
    @TableField("avg_duration")
    private Integer avgDuration;

    /**
     * 总任务数
     */
    @TableField("total_tasks")
    private Integer totalTasks = 0;

    /**
     * 已完成任务数
     */
    @TableField("completed_tasks")
    private Integer completedTasks = 0;

    /**
     * 延期任务数
     */
    @TableField("delayed_tasks")
    private Integer delayedTasks = 0;

    /**
     * 完成率（%）
     */
    @TableField("completion_rate")
    private BigDecimal completionRate;

    /**
     * 延期率（%）
     */
    @TableField("delay_rate")
    private BigDecimal delayRate;

    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("stat_date")
    private LocalDate statDate;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


