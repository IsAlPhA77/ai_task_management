package com.scu.ai_task_management.task_basic.service.impl;

import com.scu.ai_task_management.common.exception.TaskException;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import com.scu.ai_task_management.task_basic.model.TaskUpdateDTO;
import com.scu.ai_task_management.task_basic.model.TaskVO;
import com.scu.ai_task_management.task_basic.service.TaskBasicService;
import com.scu.ai_task_management.task_query.domain.TaskHistory;
import com.scu.ai_task_management.task_query.domain.TaskHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskBasicServiceImpl implements TaskBasicService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;

    @Override
    @Transactional
    public TaskVO createTask(Long userId, TaskCreateDTO createDTO) {
        log.info("创建任务: userId={}, payload={}", userId, createDTO);
        taskUtil.ensureUserId(userId);

        // 构建Task实体
        Task task = Task.builder()
                .userId(userId)
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : Task.TaskStatus.TODO)
                .category(createDTO.getCategory())
                .deadline(createDTO.getDeadline())
                .estimatedDuration(createDTO.getEstimatedDuration())
                .dependencies(createDTO.getDependencies())
                .tags(createDTO.getTags())
                .originalInput(createDTO.getOriginalInput())
                .isDeleted(false)
                .build();

        // 保存任务
        taskMapper.insert(task);

        // 记录创建历史
        taskUtil.saveTaskHistory(task.getId(), userId, TaskHistory.ActionType.CREATE, null, null, "创建任务");

        log.info("任务创建成功，ID: {}", task.getId());
        return TaskVO.fromEntity(task);
    }

    @Override
    @Transactional
    public TaskVO updateTask(Long userId, Long id, TaskUpdateDTO updateDTO) {
        log.info("更新任务，userId={}, taskId={}, 更新内容: {}", userId, id, updateDTO);

        // 查询任务
        Task task = taskUtil.getOwnedTask(userId, id);

        // 记录变更前的值
        Map<String, Object> oldValue = new HashMap<>();
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().equals(task.getTitle())) {
            oldValue.put("title", task.getTitle());
            task.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null && !updateDTO.getDescription().equals(task.getDescription())) {
            oldValue.put("description", task.getDescription());
            task.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null && updateDTO.getStatus() != task.getStatus()) {
            oldValue.put("status", task.getStatus());
            task.setStatus(updateDTO.getStatus());

            // 状态变更时处理开始时间和完成时间
            if (updateDTO.getStatus() == Task.TaskStatus.IN_PROGRESS && task.getStartTime() == null) {
                task.setStartTime(LocalDateTime.now());
            }
            if (updateDTO.getStatus() == Task.TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
                // 计算实际耗时
                if (task.getStartTime() != null) {
                    long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
                            task.getStartTime(), LocalDateTime.now());
                    task.setActualDuration((int) minutes);
                }
            }
        }
        if (updateDTO.getCategory() != null && !updateDTO.getCategory().equals(task.getCategory())) {
            oldValue.put("category", task.getCategory());
            task.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getDeadline() != null && !updateDTO.getDeadline().equals(task.getDeadline())) {
            oldValue.put("deadline", task.getDeadline());
            task.setDeadline(updateDTO.getDeadline());
        }
        if (updateDTO.getEstimatedDuration() != null && !updateDTO.getEstimatedDuration().equals(task.getEstimatedDuration())) {
            oldValue.put("estimatedDuration", task.getEstimatedDuration());
            task.setEstimatedDuration(updateDTO.getEstimatedDuration());
        }
        if (updateDTO.getActualDuration() != null && !updateDTO.getActualDuration().equals(task.getActualDuration())) {
            oldValue.put("actualDuration", task.getActualDuration());
            task.setActualDuration(updateDTO.getActualDuration());
        }
        if (updateDTO.getStartTime() != null && !updateDTO.getStartTime().equals(task.getStartTime())) {
            oldValue.put("startTime", task.getStartTime());
            task.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getDependencies() != null) {
            oldValue.put("dependencies", task.getDependencies());
            task.setDependencies(updateDTO.getDependencies());
        }
        if (updateDTO.getTags() != null) {
            oldValue.put("tags", task.getTags());
            task.setTags(updateDTO.getTags());
        }

        // 保存更新
        taskMapper.updateById(task);

        // 记录更新历史
        if (!oldValue.isEmpty()) {
            Map<String, Object> newValue = new HashMap<>();
            Task finalTask = task;
            oldValue.forEach((key, value) -> {
                try {
                    Object newVal = finalTask.getClass().getMethod("get" +
                            key.substring(0, 1).toUpperCase() + key.substring(1)).invoke(finalTask);
                    newValue.put(key, newVal);
                } catch (Exception e) {
                    log.warn("获取新值失败: {}", key, e);
                }
            });

            TaskHistory.ActionType actionType = oldValue.containsKey("status")
                    ? TaskHistory.ActionType.STATUS_CHANGE
                    : TaskHistory.ActionType.UPDATE;
            taskUtil.saveTaskHistory(task.getId(), userId, actionType, oldValue, newValue, "更新任务");
        }

        log.info("任务更新成功，ID: {}", task.getId());
        return TaskVO.fromEntity(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long id) {
        log.info("删除任务，userId={}, taskId={}", userId, id);

        Task task = taskUtil.getOwnedTask(userId, id);

        // 软删除
        task.setIsDeleted(true);
        taskMapper.updateById(task);

        // 记录删除历史
        taskUtil.saveTaskHistory(task.getId(), userId, TaskHistory.ActionType.DELETE, null, null, "删除任务");

        log.info("任务删除成功，ID: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteTasks(Long userId, List<Long> ids) {
        log.info("批量删除任务，userId={}, IDs: {}", userId, ids);
        taskUtil.ensureUserId(userId);

        List<Task> tasks = taskMapper.selectBatchIds(ids).stream()
                .filter(task -> task != null && !task.getIsDeleted() && task.getUserId().equals(userId))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            throw new TaskException("未找到要删除的任务");
        }
        if (tasks.size() != ids.size()) {
            throw new TaskException("部分任务不存在或无权删除");
        }

        // 批量软删除
        tasks.forEach(task -> {
            task.setIsDeleted(true);
            taskMapper.updateById(task);
            taskUtil.saveTaskHistory(task.getId(), userId, TaskHistory.ActionType.DELETE, null, null, "批量删除任务");
        });

        log.info("批量删除任务成功，删除数量: {}", tasks.size());
    }

}
