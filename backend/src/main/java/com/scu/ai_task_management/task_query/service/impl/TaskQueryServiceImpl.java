package com.scu.ai_task_management.task_query.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_query.model.TaskListVO;
import com.scu.ai_task_management.task_query.model.TaskQueryDTO;
import com.scu.ai_task_management.task_query.service.TaskQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskQueryServiceImpl implements TaskQueryService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;

    @Override
    public TaskVO getTaskById(Long userId, Long id) {
        log.info("查询任务，userId={}, taskId={}", userId, id);
        Task task = taskUtil.getOwnedTask(userId, id);
        return TaskVO.fromEntity(task);
    }

    @Override
    public List<TaskVO> getAllTasks(Long userId) {
        log.info("查询所有任务，userId={}", userId);
        taskUtil.ensureUserId(userId);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId);
        List<Task> tasks = taskMapper.selectList(wrapper);
        return tasks.stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public TaskListVO queryTasks(Long userId, TaskQueryDTO queryDTO) {
        log.info("分页查询任务，userId={}, 查询条件: {}", userId, queryDTO);
        taskUtil.ensureUserId(userId);

        // 构建查询条件
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId);

        // 标题模糊查询
        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(Task::getTitle, queryDTO.getTitle());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Task::getStatus, queryDTO.getStatus());
        }

        // 分类查询
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Task::getCategory, queryDTO.getCategory());
        }

        // 截止时间范围查询
        if (queryDTO.getDeadlineStart() != null) {
            wrapper.ge(Task::getDeadline, queryDTO.getDeadlineStart());
        }
        if (queryDTO.getDeadlineEnd() != null) {
            wrapper.le(Task::getDeadline, queryDTO.getDeadlineEnd());
        }

        // 最低优先级
        if (queryDTO.getMinPriority() != null) {
            wrapper.ge(Task::getPriority, queryDTO.getMinPriority());
        }

        // 只查询逾期任务
        if (Boolean.TRUE.equals(queryDTO.getOnlyOverdue())) {
            LocalDateTime now = LocalDateTime.now();
            wrapper.lt(Task::getDeadline, now)
                    .ne(Task::getStatus, Task.TaskStatus.COMPLETED);
        }

        // 构建排序
        boolean isAsc = "ASC".equalsIgnoreCase(queryDTO.getSortDirection());
        switch (queryDTO.getSortBy()) {
            case "priority":
                wrapper.orderBy(true, isAsc, Task::getPriority);
                break;
            case "deadline":
                wrapper.orderBy(true, isAsc, Task::getDeadline);
                break;
            case "createdAt":
                wrapper.orderBy(true, isAsc, Task::getCreatedAt);
                break;
            case "updatedAt":
                wrapper.orderBy(true, isAsc, Task::getUpdatedAt);
                break;
            default:
                wrapper.orderByDesc(Task::getPriority);
        }

        // 构建分页
        Page<Task> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 执行查询
        IPage<Task> pageResult = taskMapper.selectPage(page, wrapper);

        // 转换为VO
        List<TaskVO> taskVOs = pageResult.getRecords().stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());

        // 构建返回结果
        return TaskListVO.builder()
                .tasks(taskVOs)
                .total(pageResult.getTotal())
                .page(queryDTO.getPage())
                .size(queryDTO.getSize())
                .totalPages((int) pageResult.getPages())
                .build();
    }

    @Override
    public List<TaskVO> getTasksByStatus(Long userId, String status) {
        log.info("根据状态查询任务，userId={}, 状态: {}", userId, status);
        taskUtil.ensureUserId(userId);

        Task.TaskStatus taskStatus;
        try {
            taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的任务状态: " + status);
        }

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, taskStatus)
                .eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId);
        List<Task> tasks = taskMapper.selectList(wrapper);
        return tasks.stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskVO> getTodoTasksOrderByPriority(Long userId) {
        log.info("查询待办任务（按优先级排序），userId={}", userId);
        taskUtil.ensureUserId(userId);
        List<Task> tasks = taskMapper.findTodoTasksOrderByPriorityDesc(userId);
        return tasks.stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskVO> getOverdueTasks(Long userId) {
        log.info("查询逾期任务，userId={}", userId);
        taskUtil.ensureUserId(userId);
        List<Task> tasks = taskMapper.findOverdueTasks(userId, LocalDateTime.now());
        return tasks.stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskVO> getUpcomingTasks(Long userId, Integer days) {
        log.info("查询即将到期任务，userId={}, 天数: {}", userId, days);
        taskUtil.ensureUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(days);

        List<Task> tasks = taskMapper.findUpcomingTasks(userId, now, futureTime);
        return tasks.stream()
                .map(TaskVO::fromEntity)
                .collect(Collectors.toList());
    }

}
