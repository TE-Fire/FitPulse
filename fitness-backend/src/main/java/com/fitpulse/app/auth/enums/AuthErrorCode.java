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
    /** 邮箱已注册 */
    EMAIL_ALREADY_REGISTERED("409", "邮箱已注册"),
    /** 登录验证码 60 秒发送过于频繁 */
    SEND_CODE_TOO_FREQUENT("409", "发送过于频繁，请60秒后再试"),
    /** 注册验证码 60 秒发送过于频繁 */
    REGISTER_SEND_CODE_TOO_FREQUENT("409", "注册验证码发送过于频繁，请60秒后再试"),

    // ========== 400 参数类 ==========
    /** 登录 type 非法（既不是 1 密码，也不是 2 验证码） */
    INVALID_LOGIN_TYPE("400", "登录类型非法"),
    /** 密码登录分支中密码为空 */
    PASSWORD_EMPTY("400", "密码不能为空"),
    /** 验证码登录分支中验证码格式不对（非 6 位数字） */
    CODE_FORMAT_ERROR("400", "验证码格式不正确"),
    /** 注册请求中注册验证码为空 */
    REGISTER_CODE_EMPTY("400", "注册验证码不能为空"),
    /** 注册请求中注册验证码格式不对（非 6 位数字） */
    REGISTER_CODE_FORMAT_ERROR("400", "注册验证码应为6位数字"),
    /** refresh 接口提交的 token type 不是 refresh */
    NOT_REFRESH_TOKEN("400", "不是有效的refreshToken"),

    // ========== 401 未认证类 ==========
    /** 登录时邮箱不存在 / 被禁用 / 密码错误（统一提示防枚举攻击） */
    EMAIL_OR_PASSWORD_ERROR("401", "邮箱或密码错误"),
    /** Redis 中登录验证码已过期（5 分钟或不存在） */
    CODE_EXPIRED("401", "验证码已过期"),
    /** 用户输入登录验证码与 Redis 中不一致 */
    CODE_ERROR("401", "验证码错误"),
    /** Redis 中注册验证码已过期（5 分钟或不存在） */
    REGISTER_CODE_EXPIRED("401", "注册验证码已过期"),
    /** 用户输入注册验证码与 Redis 中不一致 */
    REGISTER_CODE_ERROR("401", "注册验证码错误"),
    /** refreshToken 签名不对 / 已过期 / 已登出（已在 Redis 中删除） / 被旋转使用 */
    REFRESH_TOKEN_INVALID("401", "refreshToken已失效，请重新登录"),
    /** refresh 过程中发现用户 status != 1（被禁用） */
    ACCOUNT_DISABLED("401", "账号已禁用，请重新登录");

    private final String errorCode;
    private final String errorMessage;

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
