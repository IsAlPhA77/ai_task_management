package com.scu.ai_task_management.infra.ai.service;

import com.scu.ai_task_management.infra.ai.model.TaskBatchParseResponse;
import com.scu.ai_task_management.infra.ai.model.TaskParseRequest;
import com.scu.ai_task_management.infra.ai.model.TaskParseResponse;

/**
 * AI服务接口
 */
public interface AIService {

    /**
     * 解析自然语言任务
     * 将自然语言输入解析为结构化的任务信息
     *
     * @param request 解析请求
     * @return 解析后的任务信息
     */
    TaskBatchParseResponse parseNaturalLanguageTask(TaskParseRequest request);

    /**
     * 本地保底解析策略
     *
     * @param request 解析请求
     * @return 基础解析结果
     */
    TaskParseResponse fallbackParseNaturalLanguageTask(TaskParseRequest request);
}

