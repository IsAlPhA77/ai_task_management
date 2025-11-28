package com.scu.ai_task_management.task_ai_assistant.domain;

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
 * AI调用日志Mapper
 */
@Mapper
public interface AICallLogMapper extends BaseMapper<AICallLog> {

    /**
     * 根据功能名称查询日志
     */
    default List<AICallLog> findByFunctionNameOrderByCreatedAtDesc(AICallLog.FunctionName functionName) {
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AICallLog::getFunctionName, functionName)
                .orderByDesc(AICallLog::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据功能名称分页查询日志
     */
    default IPage<AICallLog> findByFunctionNameOrderByCreatedAtDesc(Page<AICallLog> page, AICallLog.FunctionName functionName) {
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AICallLog::getFunctionName, functionName)
                .orderByDesc(AICallLog::getCreatedAt);
        return selectPage(page, wrapper);
    }

    /**
     * 根据用户ID查询日志
     */
    default List<AICallLog> findByUserIdOrderByCreatedAtDesc(Long userId) {
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AICallLog::getUserId, userId)
                .orderByDesc(AICallLog::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 根据状态查询日志
     */
    default List<AICallLog> findByStatusOrderByCreatedAtDesc(String status) {
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AICallLog::getStatus, status)
                .orderByDesc(AICallLog::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 查询指定时间范围内的日志
     */
    default List<AICallLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(AICallLog::getCreatedAt, start, end)
                .orderByDesc(AICallLog::getCreatedAt);
        return selectList(wrapper);
    }

    /**
     * 统计功能调用次数
     */
    @Select("SELECT function_name, COUNT(*) as count FROM ai_call_log GROUP BY function_name")
    List<Object[]> countCallsByFunction();

    /**
     * 统计功能调用成功率
     */
    @Select("SELECT function_name, COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(*) as success_rate FROM ai_call_log GROUP BY function_name")
    List<Object[]> calculateSuccessRateByFunction();

    /**
     * 计算平均响应时间
     */
    @Select("SELECT AVG(response_time) FROM ai_call_log WHERE function_name = #{functionName} AND status = 'SUCCESS'")
    Double calculateAvgResponseTime(@Param("functionName") String functionName);

    /**
     * 统计Token使用总量
     */
    @Select("SELECT SUM(tokens_used) FROM ai_call_log WHERE created_at BETWEEN #{startTime} AND #{endTime}")
    Long sumTokensUsed(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}


