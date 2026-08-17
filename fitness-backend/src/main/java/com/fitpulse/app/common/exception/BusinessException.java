package com.fitpulse.app.common.exception;

import com.fitpulse.app.common.enums.ErrorCodeEnum;

public class BusinessException extends BaseException {

    /**
     * 通用：接受任何实现了 BaseExceptionInterface 的错误枚举
     * （common 层的 ErrorCodeEnum 或各业务模块的专属枚举都可）。
     */
    public BusinessException(BaseExceptionInterface errorCode) {
        super(errorCode);
    }

    /**
     * 通用：接受任何实现了 BaseExceptionInterface 的错误枚举 + 自定义 message。
     */
    public BusinessException(BaseExceptionInterface errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 便捷：只传 message，code 自动使用 INTERNAL_ERROR("500")。
     */
    public BusinessException(String message) {
        super(ErrorCodeEnum.INTERNAL_ERROR, message);
    }
}
