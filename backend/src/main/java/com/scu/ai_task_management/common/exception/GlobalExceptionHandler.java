package com.scu.ai_task_management.common.exception;

import com.scu.ai_task_management.common.utils.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.scu.ai_task_management.common.constants.ExceptionConstants.BUSINESS_CODE_BAD_REQUEST;
import static com.scu.ai_task_management.common.constants.ExceptionConstants.BUSINESS_CODE_INTERNAL_SERVER_ERROR;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BaseBusinessException e) {
        log.warn("[业务异常] businessCode={}, httpStatus={}, message={}",
                e.getBusinessCode(), e.getHttpStatus().value(), e.getMessage());

        Result<Void> result = Result.error(e.getBusinessCode(), e.getMessage());

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");

        log.warn("参数校验失败: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(BUSINESS_CODE_BAD_REQUEST, "参数校验失败: " + message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");

        log.warn("参数校验失败: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(BUSINESS_CODE_BAD_REQUEST, "参数校验失败: " + message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(Exception e) {
        log.error("系统未知异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(BUSINESS_CODE_INTERNAL_SERVER_ERROR, "系统繁忙，请稍后重试"));
    }
}