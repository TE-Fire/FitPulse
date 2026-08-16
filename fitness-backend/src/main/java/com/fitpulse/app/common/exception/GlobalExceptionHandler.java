package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> biz(BusinessException e) {
        log.warn("[业务异常] code={} msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class})
    public Result<Void> auth(AuthenticationException e) {
        log.warn("[鉴权失败] {}", e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> forbidden(AccessDeniedException e) {
        return Result.fail(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> valid(Exception e) {
        String msg = "参数校验失败";
        try {
            if (e instanceof MethodArgumentNotValidException ex) {
                msg = ex.getBindingResult().getFieldError().getDefaultMessage();
            } else if (e instanceof BindException ex) {
                msg = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        } catch (Exception ignore) {}
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> cv(ConstraintViolationException e) {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> body() {
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> uploadSize() {
        return Result.fail(ResultCode.FILE_UPLOAD_FAIL.getCode(), "文件超过大小限制(100MB)");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> all(Exception e) {
        log.error("[系统异常]", e);
        return Result.fail(ResultCode.SERVER_ERROR);
    }
}
