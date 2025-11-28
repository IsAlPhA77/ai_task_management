package com.scu.ai_task_management.common.exception;

import org.springframework.http.HttpStatus;

import static edu.scu.playermanage.common.constants.ExceptionConstants.BUSINESS_CODE_BAD_REQUEST;

/**
 * 任务未找到异常
 */
public class TaskException extends BaseBusinessException {

    public TaskException(String message) {
        super(message, HttpStatus.NOT_FOUND, BUSINESS_CODE_BAD_REQUEST);
    }


    public TaskException(String message, HttpStatus httpStatus, String businessCodeBadRequest) {
        super(message, httpStatus, businessCodeBadRequest);
    }

    public static TaskException taskNotFound(){
        return new TaskException("任务不存在", HttpStatus.NOT_FOUND, BUSINESS_CODE_BAD_REQUEST);
    }
}

