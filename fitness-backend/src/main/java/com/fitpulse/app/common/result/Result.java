package com.fitpulse.app.common.result;

import com.fitpulse.app.common.enums.ErrorCodeEnum;
import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ErrorCodeEnum.SUCCESS.getCode());
        result.setMessage(ErrorCodeEnum.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCodeEnum.SUCCESS.getCode());
        result.setMessage(ErrorCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(IErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.code());
        result.setMessage(errorCode.message());
        return result;
    }

    public static <T> Result<T> fail(IErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.code());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
