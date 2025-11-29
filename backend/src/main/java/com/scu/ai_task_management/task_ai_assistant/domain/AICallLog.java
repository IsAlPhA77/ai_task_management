package com.scu.ai_task_management.task_ai_assistant.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI调用日志实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_call_log")
public class AICallLog {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 功能名称
     */
    @TableField("function_name")
    private FunctionName functionName;

    /**
     * 输入内容
     */
    @TableField("input_text")
    private String inputText;

    /**
     * 输出内容
     */
    @TableField("output_text")
    private String outputText;

    /**
     * 使用的Prompt
     */
    @TableField("prompt")
    private String prompt;

    /**
     * AI模型名称
     */
    @TableField("model")
    private String model;

    /**
     * 消耗的Token数
     */
    @TableField("tokens_used")
    private Integer tokensUsed;

    /**
     * 响应时间（毫秒）
     */
    @TableField("response_time")
    private Integer responseTime;

    /**
     * 调用状态
     */
    @TableField("status")
    private String status = "SUCCESS";

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 调用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 功能名称枚举
     */
    public enum FunctionName {
        NLP_PARSE("自然语言解析"),
        PRIORITY_CALC("优先级计算"),
        DURATION_PREDICT("耗时预测"),
        REPORT_GEN("报告生成"),
        CATEGORY_CLASSIFY("任务分类");

        private final String description;

        FunctionName(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}


