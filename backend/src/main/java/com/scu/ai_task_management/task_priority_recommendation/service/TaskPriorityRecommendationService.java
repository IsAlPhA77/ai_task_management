package com.scu.ai_task_management.task_priority_recommendation.service;

import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationDTO;
import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationVO;

import java.util.List;

/**
 * 任务优先级推荐服务接口
 */
public interface TaskPriorityRecommendationService {

    /**
     * 获取任务优先级推荐
     *
     * @param userId 用户ID
     * @param requestDTO 请求参数
     * @return 优先级推荐列表
     */
    List<PriorityRecommendationVO> getPriorityRecommendations(Long userId, PriorityRecommendationDTO requestDTO);

    /**
     * 应用优先级推荐
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param recommendedPriority 推荐优先级
     */
    void applyPriorityRecommendation(Long userId, Long taskId, Integer recommendedPriority);

    /**
     * 批量应用优先级推荐
     *
     * @param userId 用户ID
     * @param recommendations 推荐列表
     */
    void batchApplyPriorityRecommendations(Long userId, List<PriorityRecommendationVO> recommendations);

    /**
     * 重新计算所有任务优先级
     *
     * @param userId 用户ID
     */
    void recalculateAllTaskPriorities(Long userId);
}
