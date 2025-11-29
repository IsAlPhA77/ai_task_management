package com.scu.ai_task_management.task_basic.model;

import com.scu.ai_task_management.task_basic.domain.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务VO（返回给前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务信息")
public class TaskVO {

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "任务标题", example = "完成项目文档编写")
    private String title;

    @Schema(description = "任务描述", example = "编写项目的API文档和用户手册")
    private String description;

    @Schema(description = "任务状态", example = "TODO")
    private String status;

    @Schema(description = "任务状态描述", example = "待办")
    private String statusDescription;

    @Schema(description = "优先级分数", example = "85")
    private Integer priority;

    @Schema(description = "任务分类", example = "开发")
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间", example = "2025-12-01 18:00:00")
    private LocalDateTime deadline;

    @Schema(description = "预估耗时（分钟）", example = "120")
    private Integer estimatedDuration;

    @Schema(description = "实际耗时（分钟）", example = "150")
    private Integer actualDuration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间", example = "2025-11-26 14:00:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间", example = "2025-11-26 16:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "依赖任务ID列表", example = "[1, 2, 3]")
    private List<Long> dependencies;

    @Schema(description = "标签列表", example = "[\"紧急\", \"重要\"]")
    private List<String> tags;

    @Schema(description = "自然语言原始输入", example = "明天下午3点开会讨论项目进度")
    private String originalInput;

    @Schema(description = "是否逾期", example = "false")
    private Boolean isOverdue;

    @Schema(description = "距离截止时间天数", example = "5")
    private Long daysUntilDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-20 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-26 09:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从Task实体转换为TaskVO
     */
    public static TaskVO fromEntity(Task task) {
        if (task == null) {
            return null;
        }

        TaskVOBuilder builder = TaskVO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .statusDescription(task.getStatus() != null ? task.getStatus().getDescription() : null)
                .priority(task.getPriority())
                .category(task.getCategory())
                .deadline(task.getDeadline())
                .estimatedDuration(task.getEstimatedDuration())
                .actualDuration(task.getActualDuration())
                .startTime(task.getStartTime())
                .completedAt(task.getCompletedAt())
                .dependencies(task.getDependencies())
                .tags(task.getTags())
                .originalInput(task.getOriginalInput())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        // 计算是否逾期
        if (task.getDeadline() != null && task.getStatus() != Task.TaskStatus.COMPLETED) {
            builder.isOverdue(task.getDeadline().isBefore(LocalDateTime.now()));
        } else {
            builder.isOverdue(false);
        }

        // 计算距离截止时间天数
        if (task.getDeadline() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), task.getDeadline());
            builder.daysUntilDeadline(days);
        }

        return builder.build();
    }
}