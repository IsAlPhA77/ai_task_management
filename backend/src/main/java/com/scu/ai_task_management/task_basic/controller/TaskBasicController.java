package com.scu.ai_task_management.task_basic.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import com.scu.ai_task_management.task_basic.model.TaskUpdateDTO;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_basic.service.TaskBasicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务基础管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务的CRUD接口")
public class TaskBasicController {

    @Autowired
    private TaskBasicService taskBasicService;

    @PostMapping
    @Operation(summary = "创建任务", description = "创建一个新的任务")
    public Result<TaskVO> createTask(@CurrentUserId Long userId,
                                     @Valid @RequestBody TaskCreateDTO createDTO) {
        log.info("接收创建任务请求: {}", createDTO);
        TaskVO taskVO = taskBasicService.createTask(userId, createDTO);
        return Result.success(taskVO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新任务", description = "更新指定任务的信息")
    public Result<TaskVO> updateTask(
            @Parameter(description = "任务ID", required = true, example = "1")
            @PathVariable Long id,
            @CurrentUserId Long userId,
            @Valid @RequestBody TaskUpdateDTO updateDTO) {
        log.info("接收更新任务请求, ID: {}, 更新内容: {}", id, updateDTO);
        TaskVO taskVO = taskBasicService.updateTask(userId, id, updateDTO);
        return Result.success(taskVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务", description = "软删除指定任务")
    public Result<String> deleteTask(
            @Parameter(description = "任务ID", required = true, example = "1")
            @PathVariable Long id,
            @CurrentUserId Long userId) {
        log.info("接收删除任务请求, ID: {}", id);
        taskBasicService.deleteTask(userId, id);
        return Result.success("任务删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除任务", description = "批量软删除多个任务")
    public Result<String> batchDeleteTasks(
            @Parameter(description = "任务ID列表", required = true)
            @RequestBody List<Long> ids,
            @CurrentUserId Long userId) {
        log.info("接收批量删除任务请求, IDs: {}", ids);
        taskBasicService.batchDeleteTasks(userId, ids);
        return Result.success("批量删除成功");
    }
}
