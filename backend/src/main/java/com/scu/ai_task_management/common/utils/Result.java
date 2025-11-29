package com.scu.ai_task_management.common.utils;

import lombok.Data;
import org.springframework.http.HttpStatus;

import static com.scu.ai_task_management.common.constants.ExceptionConstants.BUSINESS_CODE_OK;

/**
 * 统一响应结果封装类
 */
@Data
public class Result<T> {
    private String businessCode;     // 状态码
    private String message;   // 返回消息
    private T data;           // 业务数据（可选）

    // 构造函数私有化
    private Result(String businessCode, String message, T data) {
        this.businessCode = businessCode;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(BUSINESS_CODE_OK, "操作成功", null);
    }

    /**
     * 成功（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(BUSINESS_CODE_OK, "操作成功", data);
    }

    /**
     * 成功（自定义消息 + 数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(BUSINESS_CODE_OK, message, data);
    }

    /**
     * 失败（自定义状态码 + 消息）
     */
    public static <T> Result<T> error(String businessCode, String message) {
        return new Result<>(businessCode, message, null);
    }

}