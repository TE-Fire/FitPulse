package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.enums.ErrorCodeEnum;
import com.fitpulse.app.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] code={}, message={}", e.code(), e.getMessage());
        return Result.fail(e.code(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[ValidationException] {}", message);
        return Result.fail(ErrorCodeEnum.PARAM_ERROR, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public Result<Void> handleParamParse(Exception e) {
        log.warn("[ParamParseException] {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.PARAM_ERROR, "请求参数格式错误");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentials(BadCredentialsException e) {
        log.warn("[BadCredentialsException] {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.UNAUTHORIZED, "邮箱或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthentication(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.UNAUTHORIZED, "认证失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("[UnhandledException]", e);
        return Result.fail(ErrorCodeEnum.INTERNAL_ERROR, "服务器内部错误");
    }
}
