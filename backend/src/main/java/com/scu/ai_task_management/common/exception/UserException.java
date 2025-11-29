package com.scu.ai_task_management.common.exception;

import org.springframework.http.HttpStatus;

import static com.scu.ai_task_management.common.constants.ExceptionConstants.BUSINESS_CODE_BAD_REQUEST;

/**
 * 用户相关异常
 */
public class UserException extends BaseBusinessException {

    public UserException(String message) {
        super(message, HttpStatus.BAD_REQUEST, BUSINESS_CODE_BAD_REQUEST);
    }

    public UserException(String message, HttpStatus httpStatus, String businessCode) {
        super(message, httpStatus, businessCode);
    }

    /**
     * 用户不存在
     */
    public static UserException userNotFound() {
        return new UserException("用户不存在", HttpStatus.NOT_FOUND, BUSINESS_CODE_BAD_REQUEST);
    }

    /**
     * 用户已存在
     */
    public static UserException userAlreadyExists(String field) {
        return new UserException(field + "已存在", HttpStatus.CONFLICT, BUSINESS_CODE_BAD_REQUEST);
    }

    /**
     * 密码错误
     */
    public static UserException wrongPassword() {
        return new UserException("密码错误", HttpStatus.UNAUTHORIZED, BUSINESS_CODE_BAD_REQUEST);
    }

    /**
     * 账户或密码错误
     */
    public static UserException accountOrPasswordError() {
        return new UserException("账户或密码错误", HttpStatus.UNAUTHORIZED, BUSINESS_CODE_BAD_REQUEST);
    }

    /**
     * Token 无效或已过期
     */
    public static UserException tokenInvalid() {
        return new UserException("Token无效或已过期，请重新登录", HttpStatus.UNAUTHORIZED, BUSINESS_CODE_BAD_REQUEST);
    }

    /**
     * 无权限操作
     */
    public static UserException noPermission() {
        return new UserException("无权执行该操作", HttpStatus.FORBIDDEN, BUSINESS_CODE_BAD_REQUEST);
    }
}


