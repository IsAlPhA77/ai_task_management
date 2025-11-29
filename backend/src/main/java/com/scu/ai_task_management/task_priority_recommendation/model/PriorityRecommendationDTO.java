package com.scu.ai_task_management.task_priority_recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优先级推荐请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityRecommendationDTO {

    /**
     * 任务ID列表，为空则分析所有任务
     */
    private List<Long> taskIds;

    /**
     * 是否包含已完成任务
     */
    private Boolean includeCompleted = false;

    /**
     * 是否包含已取消任务
     */
    private Boolean includeCancelled = false;

    /**
     * 重新计算优先级
     */
    private Boolean recalculate = false;
}
