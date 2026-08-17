package com.fitpulse.app.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode implements IErrorCode {

    SUCCESS(0, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    INTERNAL_ERROR(500, "服务端异常");

    private final int code;
    private final String message;
}
