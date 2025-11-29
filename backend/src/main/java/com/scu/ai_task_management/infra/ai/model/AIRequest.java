package com.scu.ai_task_management.infra.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI API请求模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 温度参数（0-2，控制随机性）
     */
    private Double temperature = 0.7;

    /**
     * 最大输出token数
     */
    private Integer maxTokens = 1000;

    /**
     * 消息模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：system, user, assistant
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}

