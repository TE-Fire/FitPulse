package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.result.ResultCode;

public class BusinessException extends BaseException {

    public BusinessException(ResultCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ResultCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(String message) {
        super(ResultCode.INTERNAL_ERROR, message);
    }
}
