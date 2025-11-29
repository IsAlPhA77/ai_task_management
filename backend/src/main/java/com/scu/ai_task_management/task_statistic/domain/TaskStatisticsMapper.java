package com.scu.ai_task_management.task_statistic.domain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 任务统计Mapper
 */
@Mapper
public interface TaskStatisticsMapper extends BaseMapper<TaskStatistics> {

    /**
     * 根据用户ID、分类和统计日期查询
     */
    default Optional<TaskStatistics> findByUserIdAndCategoryAndStatDate(Long userId, String category, LocalDate statDate) {
        LambdaQueryWrapper<TaskStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatistics::getUserId, userId)
                .eq(TaskStatistics::getCategory, category)
                .eq(TaskStatistics::getStatDate, statDate);
        TaskStatistics statistics = selectOne(wrapper);
        return Optional.ofNullable(statistics);
    }

    /**
     * 根据用户ID和统计日期查询
     */
    default List<TaskStatistics> findByUserIdAndStatDate(Long userId, LocalDate statDate) {
        LambdaQueryWrapper<TaskStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatistics::getUserId, userId)
                .eq(TaskStatistics::getStatDate, statDate);
        return selectList(wrapper);
    }

    /**
     * 根据用户ID和分类查询最新的统计数据
     */
    @Select("SELECT * FROM task_statistics WHERE user_id = #{userId} AND category = #{category} ORDER BY stat_date DESC LIMIT 1")
    Optional<TaskStatistics> findLatestByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 查询用户在指定时间范围内的统计数据
     */
    default List<TaskStatistics> findByUserIdAndStatDateBetweenOrderByStatDateDesc(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TaskStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskStatistics::getUserId, userId)
                .between(TaskStatistics::getStatDate, startDate, endDate)
                .orderByDesc(TaskStatistics::getStatDate);
        return selectList(wrapper);
    }

    /**
     * 查询用户所有分类的最新统计数据
     */
    @Select("SELECT * FROM task_statistics WHERE user_id = #{userId} AND stat_date = (SELECT MAX(stat_date) FROM task_statistics WHERE user_id = #{userId})")
    List<TaskStatistics> findLatestStatisticsByUserId(@Param("userId") Long userId);
}


