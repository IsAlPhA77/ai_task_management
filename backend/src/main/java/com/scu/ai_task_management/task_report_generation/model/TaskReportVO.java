package com.scu.ai_task_management.task_report_generation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务报告VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportVO {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 报告类型: WEEKLY-周报，MONTHLY-月报，CUSTOM-自定义
     */
    private ReportType reportType;

    /**
     * 报告开始时间
     */
    private LocalDateTime startTime;

    /**
     * 报告结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报告内容（Markdown格式）
     */
    private String content;

    /**
     * 统计数据
     */
    private ReportStatistics statistics;

    /**
     * 完成的任务列表
     */
    private List<TaskSummary> completedTasks;

    /**
     * 进行中的任务列表
     */
    private List<TaskSummary> inProgressTasks;

    /**
     * 待办任务列表
     */
    private List<TaskSummary> todoTasks;

    /**
     * 延期任务列表
     */
    private List<TaskSummary> overdueTasks;

    /**
     * AI生成的总结
     */
    private String aiSummary;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    public enum ReportType {
        WEEKLY("周报"),
        MONTHLY("月报"),
        CUSTOM("自定义报告");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportStatistics {
        /**
         * 总任务数
         */
        private Integer totalTasks;

        /**
         * 已完成任务数
         */
        private Integer completedTasks;

        /**
         * 进行中任务数
         */
        private Integer inProgressTasks;

        /**
         * 待办任务数
         */
        private Integer todoTasks;

        /**
         * 延期任务数
         */
        private Integer overdueTasks;

        /**
         * 任务完成率
         */
        private Double completionRate;

        /**
         * 总预估耗时（分钟）
         */
        private Integer totalEstimatedDuration;

        /**
         * 总实际耗时（分钟）
         */
        private Integer totalActualDuration;

        /**
         * 平均任务耗时（分钟）
         */
        private Double averageTaskDuration;

        /**
         * 按类别统计
         */
        private List<CategoryStats> categoryStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskSummary {
        /**
         * 任务ID
         */
        private Long taskId;

        /**
         * 任务标题
         */
        private String title;

        /**
         * 任务状态
         */
        private String status;

        /**
         * 任务类别
         */
        private String category;

        /**
         * 任务截止时间
         */
        private LocalDateTime Deadline;

        /**
         * 预估耗时（分钟）
         */
        private Integer estimatedDuration;

        /**
         * 实际耗时（分钟）
         */
        private Integer actualDuration;

        /**
         * 完成时间
         */
        private LocalDateTime completedAt;

        /**
         * 优先级
         */
        private Integer priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        /**
         * 类别名称
         */
        private String category;

        /**
         * 任务数量
         */
        private Integer taskCount;

        /**
         * 完成数量
         */
        private Integer completedCount;

        /**
         * 完成率
         */
        private Double completionRate;

        /**
         * 总耗时（分钟）
         */
        private Integer totalDuration;
    }
}
