package com.fitpulse.app.common.result;

import lombok.Data;
import java.io.Serializable;
import java.time.Instant;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    private Result() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = build(ResultCode.SUCCESS, data);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return build(rc, null);
    }

    public static <T> Result<T> fail(ResultCode rc, String overrideMsg) {
        Result<T> r = new Result<>();
        r.setCode(rc.getCode());
        r.setMessage(overrideMsg);
        r.setData(null);
        return r;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    private static <T> Result<T> build(ResultCode rc, T data) {
        Result<T> r = new Result<>();
        r.setCode(rc.getCode());
        r.setMessage(rc.getMessage());
        r.setData(data);
        return r;
    }
}
