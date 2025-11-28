package com.scu.ai_task_management;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class AIConnect {

    @Test
    void openAIApiExample() {
        try {
            String baseUrl = "https://api.openai.com/v1/chat/completions";
            String apiKey = "";
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String inputJson = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"Hello!\"}]}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = inputJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            // 处理响应...

        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void qwenApiTest() {
        try {
            // 方式1: 使用环境变量（推荐）
//            String apiKey = System.getenv("DASHSCOPE_API_KEY");

            // 方式2: 直接设置（不推荐，仅用于测试）
            String apiKey = "";

            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("请设置环境变量 DASHSCOPE_API_KEY");
                return;
            }

            // 创建Generation实例
            Generation generation = new Generation();

            // 构建消息
            Message userMessage = Message.builder()
                    .role(Role.USER.getValue())
                    .content("你是谁")
                    .build();

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model("qwen-turbo")  // 免费模型: qwen-turbo (推荐), qwen-plus(付费), qwen-max(付费)
                    .messages(Arrays.asList(userMessage))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .enableSearch(false)
                    .build();

            // 调用API
            GenerationResult result = generation.call(param);

            // 打印结果
            System.out.println("Request ID: " + result.getRequestId());
            System.out.println("Output: " + result.getOutput());
            System.out.println("Usage: " + result.getUsage());

            // 提取回复内容
            String answer = result.getOutput().getChoices().get(0).getMessage().getContent();
            System.out.println("\n=== AI回复 ===");
            System.out.println(answer);

        } catch (NoApiKeyException e) {
            System.err.println("API Key未设置: " + e.getMessage());
            e.printStackTrace();
        } catch (InputRequiredException e) {
            System.err.println("输入参数错误: " + e.getMessage());
            e.printStackTrace();
        } catch (ApiException e) {
            System.err.println("API调用异常: " + e.getMessage());
            System.err.println("状态码: " + e.getStatus());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("未知错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
