package com.scu.ai_task_management.task_report_generation.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.task_report_generation.model.TaskReportDTO;
import com.scu.ai_task_management.task_report_generation.model.TaskReportVO;
import com.scu.ai_task_management.task_report_generation.service.TaskReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务报告自动生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/task-reports")
@Tag(name = "任务报告生成", description = "自动生成任务周报、月报等报告")
public class TaskReportGenerationController {

    @Autowired
    private TaskReportGenerationService reportGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "生成任务报告", description = "根据指定条件生成任务报告")
    public Result<TaskReportVO> generateReport(
            @CurrentUserId Long userId,
            @RequestBody TaskReportDTO requestDTO) {

        log.info("生成任务报告: userId={}, type={}", userId, requestDTO.getReportType());
        TaskReportVO report = reportGenerationService.generateReport(userId, requestDTO);

        return Result.success(report);
    }

    @GetMapping("/weekly")
    @Operation(summary = "生成周报", description = "自动生成本周任务周报")
    public Result<TaskReportVO> generateWeeklyReport(@CurrentUserId Long userId) {
        log.info("生成周报: userId={}", userId);
        TaskReportVO report = reportGenerationService.generateWeeklyReport(userId);

        return Result.success(report);
    }

    @GetMapping("/monthly")
    @Operation(summary = "生成月报", description = "自动生成本月任务月报")
    public Result<TaskReportVO> generateMonthlyReport(@CurrentUserId Long userId) {
        log.info("生成月报: userId={}", userId);
        TaskReportVO report = reportGenerationService.generateMonthlyReport(userId);

        return Result.success(report);
    }

    @GetMapping("/history")
    @Operation(summary = "获取报告历史", description = "获取用户的历史报告列表")
    public Result<List<TaskReportVO>> getReportHistory(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取报告历史: userId={}, limit={}", userId, limit);
        List<TaskReportVO> reports = reportGenerationService.getReportHistory(userId, limit);

        return Result.success(reports);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "获取报告详情", description = "根据ID获取报告详情")
    public Result<TaskReportVO> getReportById(
            @CurrentUserId Long userId,
            @PathVariable Long reportId) {

        log.info("获取报告详情: userId={}, reportId={}", userId, reportId);
        TaskReportVO report = reportGenerationService.getReportById(userId, reportId);

        return Result.success(report);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "删除报告", description = "删除指定的报告")
    public Result<Void> deleteReport(
            @CurrentUserId Long userId,
            @PathVariable Long reportId) {

        log.info("删除报告: userId={}, reportId={}", userId, reportId);
        reportGenerationService.deleteReport(userId, reportId);

        return Result.success();
    }
}
