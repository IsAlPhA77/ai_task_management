package com.scu.ai_task_management.task_duration_prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 耗时预测VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DurationPredictionVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 当前预估耗时（分钟）
     */
    private Integer currentEstimatedDuration;

    /**
     * 预测耗时（分钟）
     */
    private Integer predictedDuration;

    /**
     * 预测置信度（0-1）
     */
    private Double confidence;

    /**
     * 预测方法
     */
    private String predictionMethod;

    /**
     * 预测依据
     */
    private List<String> predictionFactors;

    /**
     * 历史平均耗时（分钟）
     */
    private Integer historicalAverage;

    /**
     * 相似任务数量
     */
    private Integer similarTasksCount;

    /**
     * 预测准确度评估
     */
    private String accuracyAssessment;
}
