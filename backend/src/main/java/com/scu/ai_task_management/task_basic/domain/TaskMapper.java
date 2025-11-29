package com.scu.ai_task_management.task_basic.domain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 查询已逾期的未完成任务
     * 注意：@TableLogic 会自动过滤已删除的记录，但这里显式检查以确保准确性
     */
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND deadline < #{now} AND status != 'COMPLETED' AND is_deleted = false")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 查询即将到期的任务（N天内）
     * 注意：@TableLogic 会自动过滤已删除的记录，但这里显式检查以确保准确性
     */
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND deadline BETWEEN #{now} AND #{futureTime} AND status != 'COMPLETED' AND is_deleted = false")
    List<Task> findUpcomingTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime);

    /**
     * 根据优先级降序查询未删除的待办任务
     */
    default List<Task> findTodoTasksOrderByPriorityDesc(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, Task.TaskStatus.TODO)
                .eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId)
                .orderByDesc(Task::getPriority)
                .orderByAsc(Task::getDeadline);
        return selectList(wrapper);
    }

    /**
     * 根据优先级降序分页查询未删除的待办任务
     */
    default IPage<Task> findTodoTasksOrderByPriorityDesc(Page<Task> page, Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, Task.TaskStatus.TODO)
                .eq(Task::getIsDeleted, false)
                .eq(Task::getUserId, userId)
                .orderByDesc(Task::getPriority)
                .orderByAsc(Task::getDeadline);
        return selectPage(page, wrapper);
    }
}

