package com.scu.ai_task_management.task_priority_recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优先级推荐VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityRecommendationVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 当前优先级分数
     */
    private Integer currentPriority;

    /**
     * 推荐优先级分数
     */
    private Integer recommendedPriority;

    /**
     * 优先级变化
     */
    private Integer priorityChange;

    /**
     * 推荐原因
     */
    private List<String> reasons;

    /**
     * 是否需要立即处理
     */
    private Boolean urgent;

    /**
     * 截止时间压力
     */
    private String deadlinePressure;

    /**
     * 依赖关系影响
     */
    private String dependencyImpact;

    /**
     * 任务类型影响
     */
    private String categoryImpact;
}
