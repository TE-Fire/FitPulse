package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    public BusinessException(ResultCode rc, String msg) {
        super(msg);
        this.code = rc.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
