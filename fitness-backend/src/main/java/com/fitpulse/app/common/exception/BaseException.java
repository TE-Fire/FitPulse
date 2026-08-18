package com.fitpulse.app.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException implements BaseExceptionInterface {

    private final Integer code;

    public BaseException(BaseExceptionInterface errorCode) {
        super(errorCode.getErrorMessage());
        this.code = errorCode.getErrorCode();
    }

    public BaseException(BaseExceptionInterface errorCode, String message) {
        super(message);
        this.code = errorCode.getErrorCode();
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public Integer getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMessage() {
        return getMessage();
    }
}
