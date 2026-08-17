package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.result.IErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException implements IErrorCode {

    private final int code;

    public BaseException(IErrorCode errorCode) {
        super(errorCode.message());
        this.code = errorCode.code();
    }

    public BaseException(IErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.code();
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String message() {
        return getMessage();
    }
}
