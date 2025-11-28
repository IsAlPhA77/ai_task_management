package com.scu.ai_task_management.task_query.domain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务历史Mapper
 */
@Mapper
public interface TaskHistoryMapper extends BaseMapper<TaskHistory> {

    /**
     * 根据任务ID查询历史记录
     */
    default List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId) {
        LambdaQueryWrapper<TaskHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskHistory::getTaskId, taskId)
                .orderByDesc(TaskHistory::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据任务ID和操作类型查询历史记录
     */
    default List<TaskHistory> findByTaskIdAndActionOrderByCreatedAtDesc(Long taskId, TaskHistory.ActionType action) {
        LambdaQueryWrapper<TaskHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskHistory::getTaskId, taskId)
                .eq(TaskHistory::getAction, action)
                .orderByDesc(TaskHistory::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据用户ID查询历史记录
     */
    default List<TaskHistory> findByUserIdOrderByCreatedAtDesc(Long userId) {
        LambdaQueryWrapper<TaskHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskHistory::getUserId, userId)
                .orderByDesc(TaskHistory::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 查询指定时间范围内的历史记录
     */
    default List<TaskHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<TaskHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(TaskHistory::getCreatedAt, start, end)
                .orderByDesc(TaskHistory::getCreatedAt);
        return selectList(wrapper);
    }
}


