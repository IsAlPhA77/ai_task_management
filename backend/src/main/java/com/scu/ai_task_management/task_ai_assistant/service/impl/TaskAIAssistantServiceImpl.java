package com.scu.ai_task_management.task_ai_assistant.service.impl;

import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.infra.ai.model.TaskBatchParseResponse;
import com.scu.ai_task_management.infra.ai.model.TaskParseRequest;
import com.scu.ai_task_management.infra.ai.model.TaskParseResponse;
import com.scu.ai_task_management.infra.ai.service.AIService;
import com.scu.ai_task_management.task_ai_assistant.service.TaskAIAssistantService;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_query.domain.TaskHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务AI助手服务实现类
 */
@Service
@Slf4j
public class TaskAIAssistantServiceImpl implements TaskAIAssistantService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private TaskUtil taskUtil;

    @Override
    @Transactional
    public List<TaskVO> createTaskFromNaturalLanguage(Long userId, String naturalLanguage) {
        log.info("通过自然语言创建任务: userId={}, input={}", userId, naturalLanguage);
        taskUtil.ensureUserId(userId);

        // 调用AI服务解析自然语言（支持批量）
        TaskParseRequest parseRequest = TaskParseRequest.builder()
                .naturalLanguage(naturalLanguage)
                .userId(userId)
                .build();

        TaskBatchParseResponse batchResponse = aiService.parseNaturalLanguageTask(parseRequest);

        // 检查是否为批量任务
        if (batchResponse.getTasks().size() > 1) {
            log.info("识别到批量任务，共{}个任务", batchResponse.getTasks().size());
            return taskUtil.batchCreateTasksFromParse(userId, naturalLanguage, batchResponse);
        } else {
            // 单任务保持原有逻辑
            log.info("识别到单个任务");
            TaskParseResponse parseResponse = batchResponse.getTasks().get(0);
            TaskVO taskVO = taskUtil.createSingleTaskFromParse(userId, naturalLanguage, parseResponse);
            return List.of(taskVO);
        }
    }

    @Override
    @Transactional
    public TaskVO createTaskFromNaturalLanguageFallback(Long userId, String naturalLanguage) {
        log.info("通过保底策略创建任务: userId={}, input={}", userId, naturalLanguage);
        taskUtil.ensureUserId(userId);

        TaskParseRequest parseRequest = TaskParseRequest.builder()
                .naturalLanguage(naturalLanguage)
                .userId(userId)
                .build();

        TaskParseResponse parseResponse = aiService.fallbackParseNaturalLanguageTask(parseRequest);
        TaskCreateDTO createDTO = taskUtil.buildTaskCreateDTO(parseResponse, naturalLanguage);
        Task task = taskUtil.buildTaskFromCreateDTO(userId, createDTO, parseResponse.getPriority());

        taskMapper.insert(task);
        taskUtil.saveTaskHistory(task.getId(), userId,
                TaskHistory.ActionType.CREATE, null, null,
                "通过保底策略创建任务，保底置信度: " + parseResponse.getConfidence());

        log.info("保底策略任务创建成功，ID: {}, title: {}", task.getId(), task.getTitle());
        return TaskVO.fromEntity(task);
    }

}
