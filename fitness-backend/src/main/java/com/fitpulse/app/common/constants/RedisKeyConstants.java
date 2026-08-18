package com.fitpulse.app.common.constants;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    private static final String PREFIX = "fitpulse:";

    private static final String LOGIN_CODE = PREFIX + "login:code:%s";
    private static final String REGISTER_CODE = PREFIX + "register:code:%s";
    private static final String FORGOT_PASSWORD_CODE = PREFIX + "forgot-password:code:%s";
    private static final String REFRESH_TOKEN = PREFIX + "auth:refresh:%s";

    public static String buildLoginCodeKey(String email) {
        return String.format(LOGIN_CODE, email);
    }

    public static String buildRegisterCodeKey(String email) {
        return String.format(REGISTER_CODE, email);
    }

    public static String buildForgotPasswordCodeKey(String email) {
        return String.format(FORGOT_PASSWORD_CODE, email);
    }

    public static String buildRefreshTokenKey(String userId) {
        return String.format(REFRESH_TOKEN, userId);
    }
}
