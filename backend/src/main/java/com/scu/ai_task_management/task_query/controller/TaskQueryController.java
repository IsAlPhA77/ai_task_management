package com.scu.ai_task_management.task_query.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_query.model.TaskListVO;
import com.scu.ai_task_management.task_query.model.TaskQueryDTO;
import com.scu.ai_task_management.task_query.service.TaskQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务查询", description = "任务的查询接口")
public class TaskQueryController {

    @Autowired
    private TaskQueryService taskQueryService;

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情", description = "根据任务ID获取任务详细信息")
    public Result<TaskVO> getTask(
            @Parameter(description = "任务ID", required = true, example = "1")
            @PathVariable Long id,
            @CurrentUserId Long userId) {
        log.info("接收获取任务详情请求, ID: {}", id);
        TaskVO taskVO = taskQueryService.getTaskById(userId, id);
        return Result.success(taskVO);
    }

    @GetMapping
    @Operation(summary = "获取所有任务", description = "获取所有未删除的任务列表")
    public Result<List<TaskVO>> getAllTasks(@CurrentUserId Long userId) {
        log.info("接收获取所有任务请求");
        List<TaskVO> tasks = taskQueryService.getAllTasks(userId);
        return Result.success(tasks);
    }

    @PostMapping("/query")
    @Operation(summary = "分页查询任务", description = "根据条件分页查询任务列表")
    public Result<TaskListVO> queryTasks(@CurrentUserId Long userId,
                                         @Valid @RequestBody TaskQueryDTO queryDTO) {
        log.info("接收分页查询任务请求: {}", queryDTO);
        TaskListVO taskListVO = taskQueryService.queryTasks(userId, queryDTO);
        return Result.success(taskListVO);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询任务", description = "查询指定状态的所有任务")
    public Result<List<TaskVO>> getTasksByStatus(
            @Parameter(description = "任务状态", required = true, example = "TODO")
            @PathVariable String status,
            @CurrentUserId Long userId) {
        log.info("接收根据状态查询任务请求, 状态: {}", status);
        List<TaskVO> tasks = taskQueryService.getTasksByStatus(userId, status);
        return Result.success(tasks);
    }

    @GetMapping("/todo/priority")
    @Operation(summary = "获取待办任务（按优先级排序）", description = "获取所有待办任务并按优先级降序排列")
    public Result<List<TaskVO>> getTodoTasksOrderByPriority(@CurrentUserId Long userId) {
        log.info("接收获取待办任务（按优先级排序）请求");
        List<TaskVO> tasks = taskQueryService.getTodoTasksOrderByPriority(userId);
        return Result.success(tasks);
    }

    @GetMapping("/overdue")
    @Operation(summary = "获取逾期任务", description = "获取所有已逾期的未完成任务")
    public Result<List<TaskVO>> getOverdueTasks(@CurrentUserId Long userId) {
        log.info("接收获取逾期任务请求");
        List<TaskVO> tasks = taskQueryService.getOverdueTasks(userId);
        return Result.success(tasks);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "获取即将到期的任务", description = "获取N天内即将到期的未完成任务")
    public Result<List<TaskVO>> getUpcomingTasks(
            @Parameter(description = "天数", required = true, example = "7")
            @RequestParam(defaultValue = "7") Integer days,
            @CurrentUserId Long userId) {
        log.info("接收获取即将到期任务请求, 天数: {}", days);
        List<TaskVO> tasks = taskQueryService.getUpcomingTasks(userId, days);
        return Result.success(tasks);
    }

}
