package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.enums.ErrorCodeEnum;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCodeEnum errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(String message) {
        super(ErrorCodeEnum.INTERNAL_ERROR, message);
    }
}
