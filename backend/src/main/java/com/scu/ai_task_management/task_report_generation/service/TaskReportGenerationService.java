package com.scu.ai_task_management.task_report_generation.service;

import com.scu.ai_task_management.task_report_generation.model.TaskReportDTO;
import com.scu.ai_task_management.task_report_generation.model.TaskReportVO;

import java.util.List;

/**
 * 任务报告自动生成服务接口
 */
public interface TaskReportGenerationService {

    /**
     * 生成任务报告
     *
     * @param userId 用户ID
     * @param requestDTO 请求参数
     * @return 生成的报告
     */
    TaskReportVO generateReport(Long userId, TaskReportDTO requestDTO);

    /**
     * 获取用户的历史报告列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 报告列表
     */
    List<TaskReportVO> getReportHistory(Long userId, Integer limit);

    /**
     * 根据ID获取报告详情
     *
     * @param userId 用户ID
     * @param reportId 报告ID
     * @return 报告详情
     */
    TaskReportVO getReportById(Long userId, Long reportId);

    /**
     * 删除报告
     *
     * @param userId 用户ID
     * @param reportId 报告ID
     */
    void deleteReport(Long userId, Long reportId);

    /**
     * 自动生成周报（定时任务调用）
     *
     * @param userId 用户ID
     * @return 生成的周报
     */
    TaskReportVO generateWeeklyReport(Long userId);

    /**
     * 自动生成月报（定时任务调用）
     *
     * @param userId 用户ID
     * @return 生成的月报
     */
    TaskReportVO generateMonthlyReport(Long userId);
}
