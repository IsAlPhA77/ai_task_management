package com.scu.ai_task_management.task_report_generation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务报告请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportDTO {

    /**
     * 报告类型: WEEKLY-周报，MONTHLY-月报，CUSTOM-自定义
     */
    private TaskReportVO.ReportType reportType = TaskReportVO.ReportType.WEEKLY;

    /**
     * 报告开始时间（为空则根据类型自动计算）
     */
    private LocalDateTime startTime;

    /**
     * 报告结束时间（为空则根据类型自动计算）
     */
    private LocalDateTime endTime;

    /**
     * 是否包含AI总结
     */
    private Boolean includeAISummary = true;

    /**
     * 是否包含详细任务列表
     */
    private Boolean includeTaskDetails = true;

    /**
     * 是否包含统计图表数据
     */
    private Boolean includeStatistics = true;

    /**
     * 自定义报告标题
     */
    private String customTitle;

    /**
     * 报告语言: zh-CN-中文，en-US-英文
     */
    private String language = "zh-CN";
}
