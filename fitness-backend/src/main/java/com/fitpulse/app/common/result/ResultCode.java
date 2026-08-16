package com.fitpulse.app.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "ok"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务内部错误"),

    // 业务错误 1000-1999 鉴权/用户
    AUTH_WRONG(1001, "用户名或密码错误"),
    AUTH_TOKEN_INVALID(1002, "Token无效或已过期"),
    AUTH_USER_EXIST(1003, "用户名已存在"),
    AUTH_PASSWORD_NOT_MATCH(1004, "两次密码不一致"),
    AUTH_OLD_PASSWORD_WRONG(1005, "原密码错误"),

    // 2000-2999 业务资源
    BIZ_NOT_FOUND(2001, "资源不存在"),
    BIZ_DUPLICATE(2002, "数据重复"),

    // 3000-3099 文件/MinIO
    FILE_UPLOAD_FAIL(3001, "文件上传失败"),
    FILE_DELETE_FAIL(3002, "文件删除失败"),
    FILE_NOT_FOUND(3003, "文件不存在"),

    // 4000-4099 AI
    AI_CALL_FAIL(4001, "AI服务调用失败，请稍后重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
