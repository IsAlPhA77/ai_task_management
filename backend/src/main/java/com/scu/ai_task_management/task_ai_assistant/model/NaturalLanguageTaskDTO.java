package com.scu.ai_task_management.task_ai_assistant.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自然语言任务创建DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自然语言任务创建请求")
public class NaturalLanguageTaskDTO {

    @NotBlank(message = "自然语言输入不能为空")
    @Schema(description = "自然语言输入", example = "明天下午3点开会讨论项目进度", required = true)
    private String naturalLanguage;
}

