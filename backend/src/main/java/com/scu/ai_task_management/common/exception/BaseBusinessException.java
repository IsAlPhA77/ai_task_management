package com.scu.ai_task_management.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseBusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String businessCode;

    protected BaseBusinessException(String message, HttpStatus httpStatus, String businessCode) {
        super(message);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR;
        this.businessCode = businessCode;
    }
}