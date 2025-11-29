package com.scu.ai_task_management.task_duration_prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 耗时预测请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DurationPredictionRequestDTO {

    /**
     * 任务ID列表，为空则预测所有任务
     */
    private List<Long> taskIds;

    /**
     * 预测方法：SIMPLE-简单统计，AI-人工智能，HYBRID-混合方法
     */
    private PredictionMethod predictionMethod = PredictionMethod.HYBRID;

    /**
     * 最小置信度阈值（0-1）
     */
    private Double minConfidence = 0.3;

    /**
     * 是否包含历史数据分析
     */
    private Boolean includeHistoricalAnalysis = true;

    /**
     * 相似度阈值（用于查找相似任务）
     */
    private Double similarityThreshold = 0.6;

    public enum PredictionMethod {
        SIMPLE("简单统计"),
        AI("人工智能"),
        HYBRID("混合方法");

        private final String description;

        PredictionMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
