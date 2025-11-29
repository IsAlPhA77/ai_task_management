package com.scu.ai_task_management.task_ai_assistant.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_ai_assistant.model.NaturalLanguageTaskDTO;
import com.scu.ai_task_management.task_ai_assistant.service.TaskAIAssistantService;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务AI功能", description = "任务的AI辅助功能")
public class TaskAIAssistantController {

    @Autowired
    private TaskAIAssistantService taskAIAssistantService;

    @PostMapping("/natural-language")
    @Operation(summary = "通过自然语言创建任务", description = "使用AI解析自然语言输入并创建任务")
    public Result<List<TaskVO>> createTaskFromNaturalLanguage(
            @CurrentUserId Long userId,
            @RequestBody @Valid NaturalLanguageTaskDTO dto) {
        List<TaskVO> tasks = taskAIAssistantService.createTaskFromNaturalLanguage(
                userId,
                dto.getNaturalLanguage()
        );
        return Result.success(tasks);
    }

    @PostMapping("/natural-language/fallback")
    @Operation(summary = "自然语言保底策略创建任务", description = "当AI解析不可用时，使用本地正则策略创建任务")
    public Result<TaskVO> createTaskFromNaturalLanguageFallback(
            @Valid @RequestBody NaturalLanguageTaskDTO dto,
            @CurrentUserId Long userId) {
        log.info("接收自然语言保底任务创建请求: {}", dto.getNaturalLanguage());
        TaskVO taskVO = taskAIAssistantService.createTaskFromNaturalLanguageFallback(userId, dto.getNaturalLanguage());
        return Result.success("保底策略任务创建成功", taskVO);
    }

}
