package com.scu.ai_task_management.task_basic.service;

import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import com.scu.ai_task_management.task_basic.model.TaskUpdateDTO;
import com.scu.ai_task_management.task_basic.model.TaskVO;

import java.util.List;

/**
 * 任务基础服务接口
 */
public interface TaskBasicService {

    TaskVO createTask(Long userId, TaskCreateDTO createDTO);

    TaskVO updateTask(Long userId, Long id, TaskUpdateDTO updateDTO);

    void deleteTask(Long userId, Long id);

    void batchDeleteTasks(Long userId, List<Long> ids);
}
