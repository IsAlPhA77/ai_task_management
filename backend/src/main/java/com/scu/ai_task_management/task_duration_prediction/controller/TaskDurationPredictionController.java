package com.scu.ai_task_management.task_duration_prediction.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionDTO;
import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionVO;
import com.scu.ai_task_management.task_duration_prediction.service.TaskDurationPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务耗时预测控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/task-duration")
@Tag(name = "任务耗时预测", description = "基于历史数据和AI的任务耗时智能预测")
public class TaskDurationPredictionController {

    @Autowired
    private TaskDurationPredictionService durationPredictionService;

    @PostMapping("/predictions")
    @Operation(summary = "获取耗时预测", description = "为指定任务生成耗时预测")
    public Result<List<DurationPredictionVO>> getPredictions(
            @CurrentUserId Long userId,
            @RequestBody DurationPredictionDTO requestDTO) {

        log.info("获取耗时预测: userId={}", userId);
        List<DurationPredictionVO> predictions = durationPredictionService
                .getDurationPredictions(userId, requestDTO);

        return Result.success(predictions);
    }

    @GetMapping("/predict/{taskId}")
    @Operation(summary = "预测单个任务耗时", description = "为指定任务生成耗时预测")
    public Result<DurationPredictionVO> predictTaskDuration(
            @CurrentUserId Long userId,
            @PathVariable Long taskId) {

        log.info("预测单个任务耗时: userId={}, taskId={}", userId, taskId);
        DurationPredictionVO prediction = durationPredictionService.predictTaskDuration(userId, taskId);

        return Result.success(prediction);
    }

    @PostMapping("/apply/{taskId}")
    @Operation(summary = "应用耗时预测", description = "将预测的耗时应用到指定任务")
    public Result<Void> applyPrediction(
            @CurrentUserId Long userId,
            @PathVariable Long taskId,
            @RequestParam Integer predictedDuration) {

        log.info("应用耗时预测: userId={}, taskId={}, duration={}", userId, taskId, predictedDuration);
        durationPredictionService.applyDurationPrediction(userId, taskId, predictedDuration);

        return Result.success();
    }

    @PostMapping("/apply-batch")
    @Operation(summary = "批量应用耗时预测", description = "批量将预测的耗时应用到多个任务")
    public Result<Void> batchApplyPredictions(
            @CurrentUserId Long userId,
            @RequestBody List<DurationPredictionVO> predictions) {

        log.info("批量应用耗时预测: userId={}, count={}", userId, predictions.size());
        durationPredictionService.batchApplyDurationPredictions(userId, predictions);

        return Result.success();
    }

    // TODO
    @PostMapping("/train-model")
    @Operation(summary = "训练预测模型", description = "基于历史数据训练耗时预测模型")
    public Result<Void> trainPredictionModel(@CurrentUserId Long userId) {

        log.info("训练预测模型: userId={}", userId);
        durationPredictionService.trainDurationPredictionModel(userId);

        return Result.success();
    }
}
