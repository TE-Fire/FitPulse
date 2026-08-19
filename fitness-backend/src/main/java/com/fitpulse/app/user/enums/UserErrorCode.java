package com.fitpulse.app.user.enums;

import com.fitpulse.app.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User 模块专属业务错误枚举。
 * <p>每个枚举对应 user 模块内一个精确业务场景，实现 {@link BaseExceptionInterface}
 * 可直接作为 BusinessException 构造参数（无需再传第二个 message）。
 * <p>code 字段沿用 HTTP 语义大类码（400/401/403/404/409/500），前端可继续
 * 通过 code 做大类判断（如 401 → 自动跳登录），message 为精确业务文案。
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseExceptionInterface {

    // ========== 404 资源不存在 ==========
    USER_NOT_FOUND(404, "用户不存在"),
    USER_PROFILE_NOT_FOUND(404, "用户资料不存在"),

    // ========== 401 未认证/凭证失效 ==========
    OLD_PASSWORD_ERROR(401, "旧密码错误"),

    // ========== 409 业务冲突 ==========
    EMAIL_ALREADY_USED(409, "邮箱已被其他用户占用"),
    PHONE_ALREADY_USED(409, "手机号已被其他用户占用"),

    // ========== 400 参数类 ==========
    PASSWORD_CONFIRM_NOT_MATCH(400, "两次输入的密码不一致"),
    NO_FIELDS_TO_UPDATE(400, "没有需要更新的字段");

    private final Integer errorCode;
    private final String errorMessage;

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
