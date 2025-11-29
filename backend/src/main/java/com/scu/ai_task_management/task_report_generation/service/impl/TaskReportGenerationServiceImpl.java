package com.scu.ai_task_management.task_report_generation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scu.ai_task_management.common.exception.TaskException;
import com.scu.ai_task_management.common.utils.TaskUtil;
import com.scu.ai_task_management.infra.ai.model.TaskParseRequest;
import com.scu.ai_task_management.infra.ai.service.AIService;
import com.scu.ai_task_management.task_basic.domain.Task;
import com.scu.ai_task_management.task_basic.domain.TaskMapper;
import com.scu.ai_task_management.task_report_generation.model.TaskReportDTO;
import com.scu.ai_task_management.task_report_generation.model.TaskReportVO;
import com.scu.ai_task_management.task_report_generation.service.TaskReportGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务报告自动生成服务实现
 */
@Slf4j
@Service
public class TaskReportGenerationServiceImpl implements TaskReportGenerationService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskUtil taskUtil;

    @Autowired
    private AIService aiService;

    // 存储生成的报告（内存存储，实际项目中应该使用数据库）
    private final Map<Long, List<TaskReportVO>> userReports = new HashMap<>();
    private Long reportIdCounter = 1L;

    @Override
    public TaskReportVO generateReport(Long userId, TaskReportDTO requestDTO) {
        log.info("生成任务报告: userId={}, type={}", userId, requestDTO.getReportType());

        // 计算报告时间范围
        LocalDateTime[] timeRange = calculateTimeRange(requestDTO);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 获取任务数据
        List<Task> allTasks = getTasksInTimeRange(userId, startTime, endTime);
        List<Task> completedTasks = filterTasksByStatus(allTasks, Task.TaskStatus.COMPLETED);
        List<Task> inProgressTasks = filterTasksByStatus(allTasks, Task.TaskStatus.IN_PROGRESS);
        List<Task> todoTasks = filterTasksByStatus(allTasks, Task.TaskStatus.TODO);
        List<Task> overdueTasks = findOverdueTasks(allTasks, endTime);

        // 生成统计数据
        TaskReportVO.ReportStatistics statistics = generateStatistics(allTasks, completedTasks, startTime, endTime);

        // 构建任务摘要列表
        List<TaskReportVO.TaskSummary> completedTaskSummaries = completedTasks.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());

        List<TaskReportVO.TaskSummary> inProgressTaskSummaries = inProgressTasks.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());

        List<TaskReportVO.TaskSummary> todoTaskSummaries = todoTasks.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());

        List<TaskReportVO.TaskSummary> overdueTaskSummaries = overdueTasks.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());

        // 生成AI总结
        String aiSummary = requestDTO.getIncludeAISummary() ? generateAISummary(statistics, completedTasks) : null;

        // 生成报告内容
        String content = generateReportContent(requestDTO, statistics, completedTaskSummaries,
                inProgressTaskSummaries, todoTaskSummaries, overdueTaskSummaries, aiSummary);

        // 构建报告对象
        TaskReportVO report = TaskReportVO.builder()
                .id(reportIdCounter++)
                .userId(userId)
                .title(generateReportTitle(requestDTO, startTime, endTime))
                .reportType(requestDTO.getReportType())
                .startTime(startTime)
                .endTime(endTime)
                .content(content)
                .statistics(statistics)
                .completedTasks(requestDTO.getIncludeTaskDetails() ? completedTaskSummaries : null)
                .inProgressTasks(requestDTO.getIncludeTaskDetails() ? inProgressTaskSummaries : null)
                .todoTasks(requestDTO.getIncludeTaskDetails() ? todoTaskSummaries : null)
                .overdueTasks(requestDTO.getIncludeTaskDetails() ? overdueTaskSummaries : null)
                .aiSummary(aiSummary)
                .generatedAt(LocalDateTime.now())
                .build();

        // 保存报告
        userReports.computeIfAbsent(userId, k -> new ArrayList<>()).add(report);

        log.info("任务报告生成完成: userId={}, reportId={}", userId, report.getId());
        return report;
    }

    @Override
    public List<TaskReportVO> getReportHistory(Long userId, Integer limit) {
        List<TaskReportVO> reports = userReports.getOrDefault(userId, new ArrayList<>());
        return reports.stream()
                .sorted((a, b) -> b.getGeneratedAt().compareTo(a.getGeneratedAt()))
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
    }

    @Override
    public TaskReportVO getReportById(Long userId, Long reportId) {
        List<TaskReportVO> reports = userReports.getOrDefault(userId, new ArrayList<>());
        return reports.stream()
                .filter(report -> report.getId().equals(reportId))
                .findFirst()
                .orElseThrow(() -> new TaskException("报告不存在"));
    }

    @Override
    public void deleteReport(Long userId, Long reportId) {
        List<TaskReportVO> reports = userReports.get(userId);
        if (reports != null) {
            reports.removeIf(report -> report.getId().equals(reportId));
        }
        log.info("删除报告: userId={}, reportId={}", userId, reportId);
    }

    @Override
    public TaskReportVO generateWeeklyReport(Long userId) {
        TaskReportDTO requestDTO = TaskReportDTO.builder()
                .reportType(TaskReportVO.ReportType.WEEKLY)
                .includeAISummary(true)
                .includeTaskDetails(true)
                .includeStatistics(true)
                .build();

        return generateReport(userId, requestDTO);
    }

    @Override
    public TaskReportVO generateMonthlyReport(Long userId) {
        TaskReportDTO requestDTO = TaskReportDTO.builder()
                .reportType(TaskReportVO.ReportType.MONTHLY)
                .includeAISummary(true)
                .includeTaskDetails(true)
                .includeStatistics(true)
                .build();

        return generateReport(userId, requestDTO);
    }

    /**
     * 计算报告时间范围
     */
    private LocalDateTime[] calculateTimeRange(TaskReportDTO requestDTO) {
        LocalDateTime now = LocalDateTime.now();

        if (requestDTO.getStartTime() != null && requestDTO.getEndTime() != null) {
            return new LocalDateTime[]{requestDTO.getStartTime(), requestDTO.getEndTime()};
        }

        switch (requestDTO.getReportType()) {
            case WEEKLY:
                // 本周周一到周日
                LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .toLocalDate().atStartOfDay();
                LocalDateTime weekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                        .toLocalDate().atTime(23, 59, 59);
                return new LocalDateTime[]{weekStart, weekEnd};

            case MONTHLY:
                // 本月1号到月末
                LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth())
                        .toLocalDate().atStartOfDay();
                LocalDateTime monthEnd = now.with(TemporalAdjusters.lastDayOfMonth())
                        .toLocalDate().atTime(23, 59, 59);
                return new LocalDateTime[]{monthStart, monthEnd};

            default:
                // 默认最近7天
                return new LocalDateTime[]{now.minusDays(7), now};
        }
    }

    /**
     * 获取时间范围内的任务
     */
    private List<Task> getTasksInTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
                .eq(Task::getIsDeleted, false)
                .and(w -> w.between(Task::getCreatedAt, startTime, endTime)
                        .or()
                        .between(Task::getUpdatedAt, startTime, endTime)
                        .or()
                        .apply("deadline < {0} AND deadline IS NOT NULL", endTime));

        return taskMapper.selectList(wrapper);
    }


    /**
     * 按状态过滤任务
     */
    private List<Task> filterTasksByStatus(List<Task> tasks, Task.TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 查找延期任务
     */
    private List<Task> findOverdueTasks(List<Task> tasks, LocalDateTime endTime) {
        return tasks.stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isBefore(endTime))
                .filter(task -> task.getStatus() != Task.TaskStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * 生成统计数据
     */
    private TaskReportVO.ReportStatistics generateStatistics(List<Task> allTasks,
                                                             List<Task> completedTasks, LocalDateTime startTime, LocalDateTime endTime) {

        int totalTasks = allTasks.size();
        int completedCount = completedTasks.size();
        int inProgressCount = (int) allTasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        int todoCount = (int) allTasks.stream()
                .filter(task -> task.getStatus() == Task.TaskStatus.TODO).count();
        int overdueCount = (int) allTasks.stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isBefore(endTime))
                .filter(task -> task.getStatus() != Task.TaskStatus.COMPLETED)
                .count();

        double completionRate = totalTasks > 0 ? (double) completedCount / totalTasks : 0.0;

        int totalEstimatedDuration = allTasks.stream()
                .filter(task -> task.getEstimatedDuration() != null)
                .mapToInt(Task::getEstimatedDuration)
                .sum();

        int totalActualDuration = completedTasks.stream()
                .filter(task -> task.getActualDuration() != null)
                .mapToInt(Task::getActualDuration)
                .sum();

        double averageTaskDuration = completedCount > 0 ? (double) totalActualDuration / completedCount : 0.0;

        // 按类别统计
        Map<String, List<Task>> tasksByCategory = allTasks.stream()
                .filter(task -> task.getCategory() != null)
                .collect(Collectors.groupingBy(Task::getCategory));

        List<TaskReportVO.CategoryStats> categoryStats = tasksByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<Task> categoryTasks = entry.getValue();
                    int categoryTotal = categoryTasks.size();
                    int categoryCompleted = (int) categoryTasks.stream()
                            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED).count();
                    double categoryCompletionRate = categoryTotal > 0 ? (double) categoryCompleted / categoryTotal : 0.0;
                    int categoryTotalDuration = categoryTasks.stream()
                            .filter(task -> task.getActualDuration() != null)
                            .mapToInt(Task::getActualDuration)
                            .sum();

                    return TaskReportVO.CategoryStats.builder()
                            .category(category)
                            .taskCount(categoryTotal)
                            .completedCount(categoryCompleted)
                            .completionRate(categoryCompletionRate)
                            .totalDuration(categoryTotalDuration)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTaskCount(), a.getTaskCount()))
                .collect(Collectors.toList());

        return TaskReportVO.ReportStatistics.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedCount)
                .inProgressTasks(inProgressCount)
                .todoTasks(todoCount)
                .overdueTasks(overdueCount)
                .completionRate(completionRate)
                .totalEstimatedDuration(totalEstimatedDuration)
                .totalActualDuration(totalActualDuration)
                .averageTaskDuration(averageTaskDuration)
                .categoryStats(categoryStats)
                .build();
    }

    /**
     * 转换任务为摘要
     */
    private TaskReportVO.TaskSummary convertToTaskSummary(Task task) {
        return TaskReportVO.TaskSummary.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().getDescription())
                .category(task.getCategory())
                .estimatedDuration(task.getEstimatedDuration())
                .actualDuration(task.getActualDuration())
                .completedAt(task.getCompletedAt())
                .priority(task.getPriority())
                .build();
    }

    /**
     * 生成AI总结
     */
    private String generateAISummary(TaskReportVO.ReportStatistics statistics, List<Task> completedTasks) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据以下任务统计数据生成一份简洁的总结报告：\n\n");
            prompt.append("统计数据：\n");
            prompt.append("- 总任务数: ").append(statistics.getTotalTasks()).append("\n");
            prompt.append("- 已完成: ").append(statistics.getCompletedTasks()).append("\n");
            prompt.append("- 进行中: ").append(statistics.getInProgressTasks()).append("\n");
            prompt.append("- 待办: ").append(statistics.getTodoTasks()).append("\n");
            prompt.append("- 延期: ").append(statistics.getOverdueTasks()).append("\n");
            prompt.append("- 完成率: ").append(String.format("%.1f%%", statistics.getCompletionRate() * 100)).append("\n");
            prompt.append("- 总实际耗时: ").append(statistics.getTotalActualDuration()).append("分钟\n");
            prompt.append("- 平均任务耗时: ").append(String.format("%.1f", statistics.getAverageTaskDuration())).append("分钟\n\n");

            if (!completedTasks.isEmpty()) {
                prompt.append("已完成的任务：\n");
                completedTasks.stream().limit(5).forEach(task ->
                        prompt.append("- ").append(task.getTitle()).append("\n"));
            }

            prompt.append("\n请用中文写一段100字以内的总结，分析工作效率和改进建议。");

            TaskParseRequest request = TaskParseRequest.builder()
                    .naturalLanguage(prompt.toString())
                    .build();

            var response = aiService.parseNaturalLanguageTask(request);
            if (!response.getTasks().isEmpty()) {
                return response.getTasks().get(0).getDescription();
            }

        } catch (Exception e) {
            log.error("生成AI总结失败: {}", e.getMessage());
        }

        return "本周期工作总结：任务完成率" + String.format("%.1f%%", statistics.getCompletionRate() * 100) +
                "，共完成" + statistics.getCompletedTasks() + "个任务。";
    }

    /**
     * 生成报告标题
     */
    private String generateReportTitle(TaskReportDTO requestDTO, LocalDateTime startTime, LocalDateTime endTime) {
        if (requestDTO.getCustomTitle() != null) {
            return requestDTO.getCustomTitle();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        String timeRange = startTime.format(formatter) + " - " + endTime.format(formatter);

        switch (requestDTO.getReportType()) {
            case WEEKLY:
                return "任务周报 (" + timeRange + ")";
            case MONTHLY:
                return "任务月报 (" + startTime.format(DateTimeFormatter.ofPattern("yyyy年MM月")) + ")";
            default:
                return "任务报告 (" + timeRange + ")";
        }
    }

    /**
     * 生成报告内容（Markdown格式）
     */
    private String generateReportContent(TaskReportDTO requestDTO,
                                         TaskReportVO.ReportStatistics statistics,
                                         List<TaskReportVO.TaskSummary> completedTasks,
                                         List<TaskReportVO.TaskSummary> inProgressTasks,
                                         List<TaskReportVO.TaskSummary> todoTasks,
                                         List<TaskReportVO.TaskSummary> overdueTasks,
                                         String aiSummary) {

        StringBuilder content = new StringBuilder();

        // 标题
        content.append("# ").append(generateReportTitle(requestDTO, null, null)).append("\n\n");

        // 生成时间
        content.append("**生成时间:** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        // 统计概览
        if (requestDTO.getIncludeStatistics()) {
            content.append("## 统计概览\n\n");
            content.append("- **总任务数:** ").append(statistics.getTotalTasks()).append("\n");
            content.append("- **已完成:** ").append(statistics.getCompletedTasks()).append(" (")
                    .append(String.format("%.1f%%", statistics.getCompletionRate() * 100)).append(")\n");
            content.append("- **进行中:** ").append(statistics.getInProgressTasks()).append("\n");
            content.append("- **待办:** ").append(statistics.getTodoTasks()).append("\n");
            if (statistics.getOverdueTasks() > 0) {
                content.append("- **延期:** ").append(statistics.getOverdueTasks()).append("\n");
            }
            content.append("- **总预估耗时:** ").append(statistics.getTotalEstimatedDuration()).append("分钟\n");
            content.append("- **总实际耗时:** ").append(statistics.getTotalActualDuration()).append("分钟\n");
            content.append("- **平均任务耗时:** ").append(String.format("%.1f", statistics.getAverageTaskDuration())).append("分钟\n\n");
        }

        // AI总结
        if (aiSummary != null) {
            content.append("## AI总结\n\n");
            content.append(aiSummary).append("\n\n");
        }

        // 任务详情
        if (requestDTO.getIncludeTaskDetails()) {
            if (!completedTasks.isEmpty()) {
                content.append("## 已完成任务\n\n");
                completedTasks.forEach(task -> {
                    content.append("- **").append(task.getTitle()).append("**\n");
                    if (task.getCategory() != null) {
                        content.append("  - 类别: ").append(task.getCategory()).append("\n");
                    }
                    if (task.getActualDuration() != null) {
                        content.append("  - 耗时: ").append(task.getActualDuration()).append("分钟\n");
                    }
                    if (task.getCompletedAt() != null) {
                        content.append("  - 完成时间: ").append(task.getCompletedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))).append("\n");
                    }
                    content.append("\n");
                });
            }

            if (!inProgressTasks.isEmpty()) {
                content.append("## 进行中任务\n\n");
                inProgressTasks.forEach(task -> {
                    content.append("- **").append(task.getTitle()).append("**\n");
                    if (task.getCategory() != null) {
                        content.append("  - 类别: ").append(task.getCategory()).append("\n");
                    }
                    content.append("\n");
                });
            }

            if (!overdueTasks.isEmpty()) {
                content.append("## 延期任务\n\n");
                overdueTasks.forEach(task -> {
                    content.append("- **").append(task.getTitle()).append("**\n");
                    if (task.getCategory() != null) {
                        content.append("  - 类别: ").append(task.getCategory()).append("\n");
                    }
                    if (task.getDeadline() != null) {
                        content.append("  - 截止时间: ").append(task.getDeadline().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))).append("\n");
                    }
                    content.append("\n");
                });
            }
        }

        return content.toString();
    }
}
