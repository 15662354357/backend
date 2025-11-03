package com.financekb.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统异常，请联系管理员");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常：{}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        log.warn("参数验证失败：{}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "参数验证失败");
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.forbidden("权限不足");
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败：{}", e.getMessage());
        return Result.unauthorized("用户名或密码错误");
    }
}

