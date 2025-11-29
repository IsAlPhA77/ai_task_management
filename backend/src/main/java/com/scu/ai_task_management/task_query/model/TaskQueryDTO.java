package com.scu.ai_task_management.task_query.model;

import com.scu.ai_task_management.task_basic.domain.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务查询条件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务查询条件")
public class TaskQueryDTO {

    @Schema(description = "任务标题（模糊查询）", example = "项目")
    private String title;

    @Schema(description = "任务状态", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private Task.TaskStatus status;

    @Schema(description = "任务分类", example = "开发")
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间开始", example = "2025-11-01 00:00:00")
    private LocalDateTime deadlineStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间结束", example = "2025-12-31 23:59:59")
    private LocalDateTime deadlineEnd;

    @Schema(description = "最低优先级", example = "50")
    private Integer minPriority;

    @Schema(description = "是否只查询逾期任务", example = "false")
    private Boolean onlyOverdue;

    @Schema(description = "页码（从0开始）", example = "0")
    private Integer page = 0;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;

    @Schema(description = "排序字段", example = "priority", allowableValues = {"priority", "deadline", "createdAt", "updatedAt"})
    private String sortBy = "priority";

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}
