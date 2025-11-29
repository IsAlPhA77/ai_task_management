package com.scu.ai_task_management.infra.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scu.ai_task_management.common.exception.AIException;
import com.scu.ai_task_management.infra.ai.config.AIConfig;
import com.scu.ai_task_management.infra.ai.client.AIClient;
import com.scu.ai_task_management.infra.ai.model.*;
import com.scu.ai_task_management.infra.ai.service.AIService;
import com.scu.ai_task_management.common.utils.FallbackTaskUtil;
import com.scu.ai_task_management.task_ai_assistant.domain.AICallLog;
import com.scu.ai_task_management.task_ai_assistant.domain.AICallLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.scu.ai_task_management.common.constants.AIConstants.SYSTEM_PROMPT;

/**
 * AI服务实现类
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private AIClient aiClient;

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private AICallLogMapper aiCallLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 系统提示词模板
     */


    @Override
    public TaskBatchParseResponse parseNaturalLanguageTask(TaskParseRequest request) {
        log.info("开始解析自然语言任务: userId={}, input={}", request.getUserId(), request.getNaturalLanguage());

        String prompt = buildPrompt(request.getNaturalLanguage());
        List<AIConfig.ProviderConfig> providers = aiConfig.getOrderedProviders();
        List<String> errorMessages = new ArrayList<>();

        for (AIConfig.ProviderConfig providerConfig : providers) {
            String providerName = resolveProviderName(providerConfig);
            String model = resolveModel(providerConfig);

            AIRequest aiRequest = buildAIRequest(prompt, request, model);
            long startTime = System.currentTimeMillis();

            try {
                log.info("使用模型{}({})解析自然语言任务", model, providerName);
                AIResponse aiResponse = aiClient.callAI(aiRequest, providerConfig);

                String content = extractContent(aiResponse);
                TaskBatchParseResponse taskParseResponse = parseBatchResponse(content);

                long responseTime = System.currentTimeMillis() - startTime;
                saveCallLog(request, content, prompt, model, aiResponse, responseTime, "SUCCESS", null);

                return taskParseResponse;
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                saveCallLog(request, null, prompt, model, null, responseTime, "FAILED", e.getMessage());

                String message = String.format("%s: %s", providerName, e.getMessage());
                errorMessages.add(message);
                log.warn("模型{}调用失败，将尝试下一候选。原因: {}", providerName, e.getMessage());
            }
        }

        log.error("所有AI模型均调用失败: {}", String.join(" | ", errorMessages));
        throw AIException.allProvidersFailed(String.join(" | ", errorMessages));
    }

    @Override
    public TaskParseResponse fallbackParseNaturalLanguageTask(TaskParseRequest request) {
        log.info("执行保底策略解析: userId={}, input={}", request.getUserId(), request.getNaturalLanguage());
        TaskParseResponse fallback = buildFallbackResponse(request);
        try {
            saveCallLog(request,
                    objectMapper.writeValueAsString(fallback),
                    buildPrompt(request.getNaturalLanguage()),
                    "fallback-local",
                    null,
                    0L,
                    "FALLBACK_MANUAL",
                    "fallback strategy invoked");
        } catch (Exception e) {
            log.warn("保存保底日志失败", e);
        }
        return fallback;
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String naturalLanguage) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format(SYSTEM_PROMPT, currentTime);
    }

    /**
     * 提取AI响应内容
     */
    private String extractContent(AIResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw AIException.providerUnavailable("unknown", "AI响应为空");
        }

        AIResponse.Choice choice = response.getChoices().get(0);
        if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw AIException.providerUnavailable("unknown", "AI响应内容为空");
        }

        String content = choice.getMessage().getContent().trim();
        
        // 尝试提取JSON（可能包含markdown代码块）
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        
        return content.trim();
    }

    /**
     * 解析AI响应为TaskBatchParseResponse
     */
    private TaskBatchParseResponse parseBatchResponse(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);

            // 检查是否为批量任务格式
            if (jsonNode.has("tasks") && jsonNode.get("tasks").isArray()) {
                List<TaskParseResponse> tasks = new ArrayList<>();

                for (JsonNode taskNode : jsonNode.get("tasks")) {
                    tasks.add(parseSingleTask(taskNode));
                }

                Double overallConfidence = jsonNode.has("overallConfidence")
                        ? jsonNode.get("overallConfidence").asDouble()
                        : calculateAverageConfidence(tasks);

                Boolean isSingleTask = jsonNode.has("isSingleTask")
                        ? jsonNode.get("isSingleTask").asBoolean()
                        : (tasks.size() == 1);

                return TaskBatchParseResponse.builder()
                        .tasks(tasks)
                        .overallConfidence(overallConfidence)
                        .isSingleTask(isSingleTask)
                        .build();
            } else {
                // 兼容旧格式：单个任务
                TaskParseResponse singleTask = parseSingleTask(jsonNode);
                return TaskBatchParseResponse.builder()
                        .tasks(List.of(singleTask))
                        .overallConfidence(singleTask.getConfidence())
                        .isSingleTask(true)
                        .build();
            }
        } catch (Exception e) {
            log.error("解析AI响应失败: content={}", content, e);
            throw AIException.parseFailed(e.getMessage());
        }
    }

    /**
     * 保存调用日志
     */
    private void saveCallLog(TaskParseRequest request, String output, String prompt, 
                            String model, AIResponse response, long responseTime, 
                            String status, String errorMessage) {
        try {
            AICallLog callLog = AICallLog.builder()
                    .userId(request.getUserId())
                    .functionName(AICallLog.FunctionName.NLP_PARSE)
                    .inputText(request.getNaturalLanguage())
                    .outputText(output)
                    .prompt(prompt)
                    .model(model)
                    .tokensUsed(response != null && response.getUsage() != null 
                            ? response.getUsage().getTotalTokens() : null)
                    .responseTime((int) responseTime)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();

            aiCallLogMapper.insert(callLog);
        } catch (Exception e) {
            log.warn("保存AI调用日志失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    private AIRequest buildAIRequest(String prompt, TaskParseRequest request, String model) {
        return AIRequest.builder()
                .model(model)
                .messages(List.of(
                        AIRequest.Message.builder()
                                .role("system")
                                .content(prompt)
                                .build(),
                        AIRequest.Message.builder()
                                .role("user")
                                .content(request.getNaturalLanguage())
                                .build()
                ))
                .temperature(0.3)
                .maxTokens(500)
                .build();
    }

    private String resolveProviderName(AIConfig.ProviderConfig providerConfig) {
        if (providerConfig != null && providerConfig.getName() != null && !providerConfig.getName().isBlank()) {
            return providerConfig.getName();
        }
        return aiConfig.getProvider();
    }

    private String resolveModel(AIConfig.ProviderConfig providerConfig) {
        if (providerConfig != null && providerConfig.getModel() != null && !providerConfig.getModel().isBlank()) {
            return providerConfig.getModel();
        }
        return aiConfig.getModel();
    }

    /**
     * 本地保底策略：将用户输入转换为基础任务
     */
    private TaskParseResponse buildFallbackResponse(TaskParseRequest request) {
        return FallbackTaskUtil.parse(request.getNaturalLanguage());
    }

    private TaskParseResponse parseSingleTask(JsonNode jsonNode) {
        TaskParseResponse.TaskParseResponseBuilder builder = TaskParseResponse.builder();

        if (jsonNode.has("title")) {
            builder.title(jsonNode.get("title").asText());
        }
        if (jsonNode.has("description")) {
            builder.description(jsonNode.get("description").asText());
        }
        if (jsonNode.has("status")) {
            builder.status(jsonNode.get("status").asText());
        } else {
            builder.status("TODO");
        }
        if (jsonNode.has("category")) {
            builder.category(jsonNode.get("category").asText());
        }
        if (jsonNode.has("deadline") && !jsonNode.get("deadline").isNull()) {
            String deadlineStr = jsonNode.get("deadline").asText();
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            builder.deadline(deadline);
        }
        if (jsonNode.has("estimatedDuration")) {
            builder.estimatedDuration(jsonNode.get("estimatedDuration").asInt());
        }
        if (jsonNode.has("tags")) {
            List<String> tags = new ArrayList<>();
            jsonNode.get("tags").forEach(tag -> tags.add(tag.asText()));
            builder.tags(tags);
        }
        if (jsonNode.has("priority")) {
            builder.priority(jsonNode.get("priority").asInt());
        }
        if (jsonNode.has("confidence")) {
            builder.confidence(jsonNode.get("confidence").asDouble());
        } else {
            builder.confidence(0.8);
        }

        return builder.build();
    }

    private Double calculateAverageConfidence(List<TaskParseResponse> tasks) {
        return tasks.stream()
                .mapToDouble(TaskParseResponse::getConfidence)
                .average()
                .orElse(0.8);
    }
}

