package com.scu.ai_task_management.task_ai_assistant.model;

import com.scu.ai_task_management.task_basic.model.TaskCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建任务DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量创建任务请求")
public class TaskBatchCreateDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", required = true)
    private Long userId;

    @NotEmpty(message = "任务列表不能为空")
    @Valid
    @Schema(description = "任务列表", required = true)
    private List<TaskCreateDTO> tasks;

    @Schema(description = "自然语言原始输入", example = "今天下午3点足球赛对战水工队，4点对战足协队")
    private String originalInput;

    @Schema(description = "AI解析的总体置信度", example = "0.85")
    private Double overallConfidence;
}