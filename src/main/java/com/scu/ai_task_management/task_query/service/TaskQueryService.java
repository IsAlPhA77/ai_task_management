package com.scu.ai_task_management.task_query.service;

import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_query.model.TaskListVO;
import com.scu.ai_task_management.task_query.model.TaskQueryDTO;

import java.util.List;

public interface TaskQueryService {

    TaskVO getTaskById(Long userId, Long id);

    List<TaskVO> getAllTasks(Long userId);

    TaskListVO queryTasks(Long userId, TaskQueryDTO queryDTO);

    List<TaskVO> getTasksByStatus(Long userId, String status);

    List<TaskVO> getTodoTasksOrderByPriority(Long userId);

    List<TaskVO> getOverdueTasks(Long userId);

    List<TaskVO> getUpcomingTasks(Long userId, Integer days);
}
