package com.fitpulse.app.common.result;

import com.fitpulse.app.common.enums.ErrorCodeEnum;
import com.fitpulse.app.common.exception.BaseExceptionInterface;
import com.fitpulse.app.common.exception.BusinessException;
import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ErrorCodeEnum.SUCCESS.getErrorCode());
        result.setMessage(ErrorCodeEnum.SUCCESS.getErrorMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCodeEnum.SUCCESS.getErrorCode());
        result.setMessage(ErrorCodeEnum.SUCCESS.getErrorMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(BaseExceptionInterface errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getErrorCode());
        result.setMessage(errorCode.getErrorMessage());
        return result;
    }

    public static <T> Result<T> fail(BaseExceptionInterface errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getErrorCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(BusinessException e) {
        Result<T> result = new Result<>();
        result.setCode(e.getErrorCode());
        result.setMessage(e.getErrorMessage());
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
