package com.scu.ai_task_management.common.exception;

import org.springframework.http.HttpStatus;

import static com.scu.ai_task_management.common.constants.ExceptionConstants.BUSINESS_CODE_BAD_REQUEST;

/**
 * AI 相关业务异常
 */
public class AIException extends BaseBusinessException {

    public AIException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, BUSINESS_CODE_BAD_REQUEST);
    }

    public AIException(String message, HttpStatus status, String businessCode) {
        super(message, status, businessCode);
    }

    public static AIException providerUnavailable(String providerName, String detail) {
        return new AIException(String.format("AI服务 [%s] 不可用：%s", providerName, detail));
    }

    public static AIException parseFailed(String detail) {
        return new AIException("AI响应解析失败：" + detail);
    }

    public static AIException fallbackFailed() {
        return new AIException("无法生成任务内容，请稍后重试");
    }

    public static AIException allProvidersFailed(String detail) {
        return new AIException("所有AI服务暂不可用：" + detail);
    }
}

