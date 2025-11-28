package com.scu.ai_task_management.common.utils;

import com.scu.ai_task_management.common.exception.TaskException;
import com.scu.ai_task_management.infra.ai.model.TaskBatchParseResponse;
import com.scu.ai_task_management.infra.ai.model.TaskParseResponse;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_query.domain.TaskHistory;
import com.scu.ai_task_management.task_query.domain.TaskHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TaskUtil {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskHistoryMapper taskHistoryMapper;

    public void saveTaskHistory(Long taskId, Long userId, TaskHistory.ActionType action,
                                 Map<String, Object> oldValue, Map<String, Object> newValue,
                                 String remark) {
        TaskHistory history = TaskHistory.builder()
                .taskId(taskId)
                .userId(userId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .remark(remark)
                .build();
        taskHistoryMapper.insert(history);
    }

    public Task getOwnedTask(Long userId, Long taskId) {
        ensureUserId(userId);
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getIsDeleted() || !userId.equals(task.getUserId())) {
            throw TaskException.taskNotFound();
        }
        return task;
    }

    public void ensureUserId(Long userId) {
        if (userId == null) {
            throw new TaskException("未获取到当前用户");
        }
    }

    public TaskCreateDTO buildTaskCreateDTO(TaskParseResponse parseResponse, String naturalLanguage) {
        return TaskCreateDTO.builder()
                .title(StringUtils.hasText(parseResponse.getTitle()) ? parseResponse.getTitle() : naturalLanguage)
                .description(StringUtils.hasText(parseResponse.getDescription()) ? parseResponse.getDescription() : naturalLanguage)
                .status(resolveStatus(parseResponse.getStatus()))
                .category(parseResponse.getCategory())
                .deadline(parseResponse.getDeadline())
                .estimatedDuration(parseResponse.getEstimatedDuration())
                .tags(parseResponse.getTags())
                .originalInput(naturalLanguage)
                .build();
    }

    public Task buildTaskFromCreateDTO(Long userId, TaskCreateDTO createDTO, Integer priority) {
        return Task.builder()
                .userId(userId)
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : Task.TaskStatus.TODO)
                .category(createDTO.getCategory())
                .deadline(createDTO.getDeadline())
                .estimatedDuration(createDTO.getEstimatedDuration())
                .tags(createDTO.getTags())
                .originalInput(createDTO.getOriginalInput())
                .priority(priority != null ? priority : 0)
                .isDeleted(false)
                .build();
    }

    public Task.TaskStatus resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Task.TaskStatus.TODO;
        }
        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskStatus.TODO;
        }
    }

    public TaskVO createSingleTaskFromParse(Long userId, String naturalLanguage, TaskParseResponse parseResponse) {
        TaskCreateDTO createDTO = buildTaskCreateDTO(parseResponse, naturalLanguage);
        Task task = buildTaskFromCreateDTO(userId, createDTO, parseResponse.getPriority());

        // 保存任务
        taskMapper.insert(task);

        // 记录创建历史
        saveTaskHistory(task.getId(), userId,
                TaskHistory.ActionType.CREATE, null, null,
                "通过自然语言创建任务，置信度: " + parseResponse.getConfidence());

        log.info("任务创建成功，ID: {}, title: {}", task.getId(), task.getTitle());
        return TaskVO.fromEntity(task);
    }

    public List<TaskVO> batchCreateTasksFromParse(Long userId, String naturalLanguage, TaskBatchParseResponse batchResponse) {
        List<TaskVO> createdTasks = new ArrayList<>();
        List<TaskParseResponse> parsedTasks = batchResponse.getTasks();

        log.info("开始批量创建{}个任务", parsedTasks.size());

        for (int i = 0; i < parsedTasks.size(); i++) {
            TaskParseResponse parseResponse = parsedTasks.get(i);

            try {
                // 构建任务DTO
                TaskCreateDTO createDTO = buildTaskCreateDTO(parseResponse, naturalLanguage);
                Task task = buildTaskFromCreateDTO(userId, createDTO, parseResponse.getPriority());

                // 保存任务
                taskMapper.insert(task);

                // 记录创建历史
                String historyNote = String.format(
                        "通过自然语言批量创建任务 (%d/%d)，置信度: %.2f",
                        i + 1,
                        parsedTasks.size(),
                        parseResponse.getConfidence()
                );
                saveTaskHistory(task.getId(), userId,
                        TaskHistory.ActionType.CREATE, null, null, historyNote);

                createdTasks.add(TaskVO.fromEntity(task));
                log.info("任务 {}/{} 创建成功，ID: {}, title: {}",
                        i + 1, parsedTasks.size(), task.getId(), task.getTitle());

            } catch (Exception e) {
                log.error("创建任务 {}/{} 失败: {}", i + 1, parsedTasks.size(), e.getMessage(), e);
                // 继续创建其他任务，不中断整个流程
            }
        }

        log.info("批量任务创建完成，成功: {}/{}", createdTasks.size(), parsedTasks.size());

        if (createdTasks.isEmpty()) {
            throw new RuntimeException("批量任务创建全部失败");
        }

        return createdTasks;
    }
}
