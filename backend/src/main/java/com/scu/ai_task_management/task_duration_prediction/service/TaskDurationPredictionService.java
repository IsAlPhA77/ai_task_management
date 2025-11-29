package com.scu.ai_task_management.task_duration_prediction.service;

import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionDTO;
import com.scu.ai_task_management.task_duration_prediction.model.DurationPredictionVO;

import java.util.List;

/**
 * 任务耗时预测服务接口
 */
public interface TaskDurationPredictionService {

    /**
     * 获取任务耗时预测
     *
     * @param userId 用户ID
     * @param requestDTO 请求参数
     * @return 耗时预测列表
     */
    List<DurationPredictionVO> getDurationPredictions(Long userId, DurationPredictionDTO requestDTO);

    /**
     * 为单个任务生成耗时预测
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 耗时预测结果
     */
    DurationPredictionVO predictTaskDuration(Long userId, Long taskId);

    /**
     * 应用耗时预测到任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param predictedDuration 预测耗时（分钟）
     */
    void applyDurationPrediction(Long userId, Long taskId, Integer predictedDuration);

    /**
     * 批量应用耗时预测
     *
     * @param userId 用户ID
     * @param predictions 预测结果列表
     */
    void batchApplyDurationPredictions(Long userId, List<DurationPredictionVO> predictions);

    /**
     * 训练耗时预测模型
     *
     * @param userId 用户ID
     */
    void trainDurationPredictionModel(Long userId);
}
