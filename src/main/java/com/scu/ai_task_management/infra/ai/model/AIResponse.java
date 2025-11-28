package com.scu.ai_task_management.infra.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI API响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 对象类型
     */
    private String object;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 使用情况
     */
    private Usage usage;

    /**
     * 选择项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息
         */
        private AIRequest.Message message;

        /**
         * 完成原因
         */
        private String finishReason;
    }

    /**
     * Token使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 提示token数
         */
        private Integer promptTokens;

        /**
         * 完成token数
         */
        private Integer completionTokens;

        /**
         * 总token数
         */
        private Integer totalTokens;
    }
}

