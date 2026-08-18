package com.fitpulse.app.common.enums;

import com.fitpulse.app.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举（只放错误场景，不含成功状态）。
 * <p>HTTP 语义大类码：4xx 客户端 / 5xx 服务端；成功码 200 由 Result.SUCCESS_CODE 单独定义。
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum implements BaseExceptionInterface {

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    INTERNAL_ERROR(500, "服务端异常");

    private final Integer code;
    private final String message;

    @Override
    public Integer getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }
}
