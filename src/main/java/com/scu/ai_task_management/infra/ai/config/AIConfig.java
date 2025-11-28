package com.scu.ai_task_management.infra.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * AI配置类
 * 用于管理AI API的配置信息
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * AI服务提供商（openai, qwen, etc.）
     */
    private String provider = "qwen";

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 使用的模型名称
     */
    private String model = "qwen-turbo";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 多模型降级配置，按顺序依次尝试
     */
    private List<ProviderConfig> providers;

    /**
     * 返回配置好的模型列表（若未配置则回落到旧版单模型配置）
     */
    public List<ProviderConfig> getOrderedProviders() {
        if (providers != null && !providers.isEmpty()) {
            return providers;
        }
        ProviderConfig defaultConfig = new ProviderConfig();
        defaultConfig.setName(provider);
        defaultConfig.setBaseUrl(baseUrl);
        defaultConfig.setApiKey(apiKey);
        defaultConfig.setModel(model);
        defaultConfig.setTimeout(timeout);
        defaultConfig.setMaxRetries(maxRetries);
        return Collections.singletonList(defaultConfig);
    }

    @Data
    public static class ProviderConfig {
        /**
         * 提供商名称，如 openai、qwen
         */
        private String name;

        /**
         * 专用基础URL
         */
        private String baseUrl;

        /**
         * 专用API Key
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model;

        /**
         * 单独超时设置（可选）
         */
        private Integer timeout;

        /**
         * 单独重试次数（可选）
         */
        private Integer maxRetries;
    }
}

