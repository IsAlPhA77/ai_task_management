package com.scu.ai_task_management.infra.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务解析请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskParseRequest {

    /**
     * 自然语言输入
     */
    private String naturalLanguage;

    /**
     * 用户ID（用于上下文）
     */
    private Long userId;
}

