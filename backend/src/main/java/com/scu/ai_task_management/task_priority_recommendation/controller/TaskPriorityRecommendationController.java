package com.scu.ai_task_management.task_priority_recommendation.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationDTO;
import com.scu.ai_task_management.task_priority_recommendation.model.PriorityRecommendationVO;
import com.scu.ai_task_management.task_priority_recommendation.service.TaskPriorityRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务优先级推荐控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/task-priority")
@Tag(name = "任务优先级推荐", description = "任务优先级智能推荐功能")
public class TaskPriorityRecommendationController {

    @Autowired
    private TaskPriorityRecommendationService priorityRecommendationService;

    @PostMapping("/recommendations")
    @Operation(summary = "获取优先级推荐", description = "根据任务特征智能推荐优先级排序")
    public Result<List<PriorityRecommendationVO>> getRecommendations(
            @CurrentUserId Long userId,
            @RequestBody PriorityRecommendationDTO requestDTO) {

        log.info("获取优先级推荐: userId={}", userId);
        List<PriorityRecommendationVO> recommendations = priorityRecommendationService
                .getPriorityRecommendations(userId, requestDTO);

        return Result.success(recommendations);
    }

    @PostMapping("/apply/{taskId}")
    @Operation(summary = "应用优先级推荐", description = "将推荐的优先级应用到指定任务")
    public Result<Void> applyRecommendation(
            @CurrentUserId Long userId,
            @PathVariable Long taskId,
            @RequestParam Integer recommendedPriority) {

        log.info("应用优先级推荐: userId={}, taskId={}, priority={}", userId, taskId, recommendedPriority);
        priorityRecommendationService.applyPriorityRecommendation(userId, taskId, recommendedPriority);

        return Result.success();
    }

    @PostMapping("/apply-batch")
    @Operation(summary = "批量应用优先级推荐", description = "批量将推荐的优先级应用到多个任务")
    public Result<Void> batchApplyRecommendations(
            @CurrentUserId Long userId,
            @RequestBody List<PriorityRecommendationVO> recommendations) {

        log.info("批量应用优先级推荐: userId={}, count={}", userId, recommendations.size());
        priorityRecommendationService.batchApplyPriorityRecommendations(userId, recommendations);

        return Result.success();
    }

    @PostMapping("/recalculate-all")
    @Operation(summary = "重新计算所有任务优先级", description = "重新计算用户所有任务的优先级")
    public Result<Void> recalculateAllPriorities(@CurrentUserId Long userId) {
        log.info("重新计算所有任务优先级: userId={}", userId);
        priorityRecommendationService.recalculateAllTaskPriorities(userId);

        return Result.success();
    }
}
