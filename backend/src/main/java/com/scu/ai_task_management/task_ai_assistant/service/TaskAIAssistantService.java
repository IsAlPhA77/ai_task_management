package com.scu.ai_task_management.task_ai_assistant.service;

import com.scu.ai_task_management.task_basic.model.TaskVO;

import java.util.List;

/**
 * 任务AI助手服务接口
 */
public interface TaskAIAssistantService {

    List<TaskVO> createTaskFromNaturalLanguage(Long userId, String naturalLanguage);

    TaskVO createTaskFromNaturalLanguageFallback(Long userId, String naturalLanguage);

}
