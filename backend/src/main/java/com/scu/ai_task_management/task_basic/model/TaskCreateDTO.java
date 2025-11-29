package com.scu.ai_task_management.task_basic.model;

import com.scu.ai_task_management.task_basic.domain.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建任务DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建任务请求")
public class TaskCreateDTO {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过200个字符")
    @Schema(description = "任务标题", example = "完成项目文档编写", required = true)
    private String title;

    @Schema(description = "任务描述", example = "编写项目的API文档和用户手册")
    private String description;

    @Schema(description = "任务状态", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private Task.TaskStatus status;

    @Schema(description = "任务分类", example = "开发")
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止时间", example = "2025-12-01 18:00:00")
    private LocalDateTime deadline;

    @Schema(description = "预估耗时（分钟）", example = "120")
    private Integer estimatedDuration;

    @Schema(description = "依赖任务ID列表", example = "[1, 2, 3]")
    private List<Long> dependencies;

    @Schema(description = "标签列表", example = "[\"紧急\", \"重要\"]")
    private List<String> tags;

    @Schema(description = "自然语言原始输入", example = "明天下午3点开会讨论项目进度")
    private String originalInput;
}
