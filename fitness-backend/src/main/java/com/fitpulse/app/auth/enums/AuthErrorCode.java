package com.fitpulse.app.auth.enums;

import com.fitpulse.app.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Auth 模块专属业务错误枚举。
 * <p>每个枚举对应 auth 模块内一个精确业务场景，实现 {@link BaseExceptionInterface}
 * 可直接作为 BusinessException 构造参数（无需再传第二个 message）。
 * <p>code 字段沿用 HTTP 语义大类码（400/401/403/409/500），前端可继续
 * 通过 code 做大类判断（如 401 → 自动跳登录），message 为精确业务文案。
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseExceptionInterface {

    // ========== 409 冲突类 ==========
    EMAIL_ALREADY_REGISTERED(409, "邮箱已注册"),
    SEND_CODE_TOO_FREQUENT(409, "发送过于频繁，请60秒后再试"),
    REGISTER_SEND_CODE_TOO_FREQUENT(409, "注册验证码发送过于频繁，请60秒后再试"),

    // ========== 400 参数类 ==========
    INVALID_LOGIN_TYPE(400, "登录类型非法"),
    PASSWORD_EMPTY(400, "密码不能为空"),
    CODE_FORMAT_ERROR(400, "验证码格式不正确"),
    REGISTER_CODE_EMPTY(400, "注册验证码不能为空"),
    REGISTER_CODE_FORMAT_ERROR(400, "注册验证码应为6位数字"),
    NOT_REFRESH_TOKEN(400, "不是有效的refreshToken"),

    // ========== 401 未认证类 ==========
    EMAIL_OR_PASSWORD_ERROR(401, "邮箱或密码错误"),
    CODE_EXPIRED(401, "验证码已过期"),
    CODE_ERROR(401, "验证码错误"),
    REGISTER_CODE_EXPIRED(401, "注册验证码已过期"),
    REGISTER_CODE_ERROR(401, "注册验证码错误"),
    REFRESH_TOKEN_INVALID(401, "refreshToken已失效，请重新登录"),
    ACCOUNT_DISABLED(401, "账号已禁用，请重新登录");

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
