package com.scu.ai_task_management.infra.ai.client;

import com.scu.ai_task_management.infra.ai.config.AIConfig;
import com.scu.ai_task_management.infra.ai.model.AIRequest;
import com.scu.ai_task_management.infra.ai.model.AIResponse;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI客户端 - 支持多个AI Provider
 */
@Slf4j
@Component
public class AIClient {

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    /**
     * 调用AI API
     */
    public AIResponse callAI(AIRequest request, AIConfig.ProviderConfig providerConfig) {
        AIConfig.ProviderConfig effectiveConfig = providerConfig != null
                ? providerConfig
                : new AIConfig.ProviderConfig();
        String providerName = resolveProviderName(effectiveConfig);

        log.info("调用AI API: provider={}, Model={}", providerName, request.getModel());
        log.debug("请求详情: {}", request);

        try {
            long startTime = System.currentTimeMillis();
            AIResponse aiResponse;

            // 根据 provider 选择调用方式
            if ("qwen".equalsIgnoreCase(providerName) || "dashscope".equalsIgnoreCase(providerName)) {
                aiResponse = callQwenWithSDK(request, effectiveConfig);
            } else if ("openai".equalsIgnoreCase(providerName)) {
                aiResponse = callOpenAIWithHTTP(request, effectiveConfig);
            } else {
                throw new IllegalArgumentException("不支持的 AI Provider: " + providerName);
            }

            long responseTime = System.currentTimeMillis() - startTime;

            if (aiResponse != null && aiResponse.getUsage() != null) {
                log.info("AI API调用成功: provider={}, Tokens={}, 耗时={}ms",
                        providerName, aiResponse.getUsage().getTotalTokens(), responseTime);
            }

            return aiResponse;
        } catch (Exception e) {
            log.error("AI API调用失败 (provider={}): {}", providerName, e.getMessage(), e);
            throw new RuntimeException("AI API调用失败 [" + providerName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 DashScope SDK 调用 Qwen
     */
    private AIResponse callQwenWithSDK(AIRequest request, AIConfig.ProviderConfig providerConfig)
            throws ApiException, NoApiKeyException, InputRequiredException {

        String apiKey = resolveApiKey(providerConfig);
        log.debug("Qwen API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));

        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
                .model(request.getModel())
                .apiKey(apiKey)
                .messages(convertToDashScopeMessages(request.getMessages()))
                .temperature(request.getTemperature() != null ? request.getTemperature().floatValue() : 0.7f)
                .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 1000)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        GenerationResult result = gen.call(param);
        return convertDashScopeToAIResponse(result, request.getModel());
    }

    /**
     * 使用 HTTP 调用 OpenAI API
     */
    private AIResponse callOpenAIWithHTTP(AIRequest request, AIConfig.ProviderConfig providerConfig)
            throws Exception {

        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        String apiKey = resolveApiKey(providerConfig);
        String baseUrl = providerConfig.getBaseUrl() != null
                ? providerConfig.getBaseUrl()
                : "https://api.openai.com/v1/chat/completions";

        log.debug("OpenAI URL: {}", baseUrl);
        log.debug("OpenAI API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.getModel());
        requestBody.put("messages", request.getMessages());
        if (request.getTemperature() != null) {
            requestBody.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            requestBody.put("max_tokens", request.getMaxTokens());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.debug("发送 OpenAI 请求: {}", objectMapper.writeValueAsString(requestBody));

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        log.debug("OpenAI 响应状态: {}", response.getStatusCode());
        log.debug("OpenAI 响应体: {}", response.getBody());

        // 解析响应
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.readValue(response.getBody(), AIResponse.class);
        } else {
            throw new RuntimeException("OpenAI API 调用失败: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    /**
     * 转换消息格式为DashScope格式
     */
    private List<Message> convertToDashScopeMessages(List<AIRequest.Message> messages) {
        List<Message> dashScopeMessages = new ArrayList<>();
        for (AIRequest.Message msg : messages) {
            String roleStr;
            switch (msg.getRole().toLowerCase()) {
                case "system":
                    roleStr = "system";
                    break;
                case "user":
                    roleStr = "user";
                    break;
                case "assistant":
                    roleStr = "assistant";
                    break;
                default:
                    roleStr = "user";
            }
            dashScopeMessages.add(Message.builder()
                    .role(roleStr)
                    .content(msg.getContent())
                    .build());
        }
        return dashScopeMessages;
    }

    /**
     * 将DashScope结果转换为AIResponse
     */
    private AIResponse convertDashScopeToAIResponse(GenerationResult result, String model) {
        if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null) {
            throw new RuntimeException("DashScope 返回结果为空");
        }

        List<AIResponse.Choice> choices = new ArrayList<>();
        for (int i = 0; i < result.getOutput().getChoices().size(); i++) {
            var choice = result.getOutput().getChoices().get(i);
            AIRequest.Message message = AIRequest.Message.builder()
                    .role("assistant")
                    .content(choice.getMessage().getContent())
                    .build();

            choices.add(AIResponse.Choice.builder()
                    .index(i)
                    .message(message)
                    .finishReason(choice.getFinishReason())
                    .build());
        }

        AIResponse.Usage usage = null;
        if (result.getUsage() != null) {
            usage = AIResponse.Usage.builder()
                    .promptTokens(result.getUsage().getInputTokens())
                    .completionTokens(result.getUsage().getOutputTokens())
                    .totalTokens(result.getUsage().getTotalTokens())
                    .build();
        }

        return AIResponse.builder()
                .id(result.getRequestId())
                .object("text_completion")
                .created(System.currentTimeMillis() / 1000)
                .model(model)
                .choices(choices)
                .usage(usage)
                .build();
    }

    private String resolveProviderName(AIConfig.ProviderConfig providerConfig) {
        if (providerConfig != null && providerConfig.getName() != null && !providerConfig.getName().isBlank()) {
            return providerConfig.getName();
        }
        return aiConfig.getProvider();
    }

    private String resolveApiKey(AIConfig.ProviderConfig providerConfig) {
        String apiKey = null;
        if (providerConfig != null && providerConfig.getApiKey() != null && !providerConfig.getApiKey().isBlank()) {
            apiKey = providerConfig.getApiKey();
        } else {
            apiKey = aiConfig.getApiKey();
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("API Key 未配置");
        }

        return apiKey;
    }
}