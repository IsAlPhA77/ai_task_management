package com.scu.ai_task_management.infra.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务解析响应DTO
 * AI解析自然语言后返回的结构化任务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskParseResponse {

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务状态（TODO, IN_PROGRESS, COMPLETED, CANCELLED）
     */
    private String status;

    /**
     * 任务分类
     */
    private String category;

    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /**
     * 预估耗时（分钟）
     */
    private Integer estimatedDuration;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 优先级分数（0-100）
     */
    private Integer priority;

    /**
     * 解析置信度（0-1）
     */
    private Double confidence;
}

