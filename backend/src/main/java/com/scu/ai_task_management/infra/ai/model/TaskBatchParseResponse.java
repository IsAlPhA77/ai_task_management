package com.scu.ai_task_management.infra.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI批量任务解析响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskBatchParseResponse {

    /**
     * 解析出的任务列表
     */
    private List<TaskParseResponse> tasks;

    /**
     * 总体置信度
     */
    private Double overallConfidence;

    /**
     * 是否为单任务（兼容性字段）
     */
    @Builder.Default
    private Boolean isSingleTask = false;
}