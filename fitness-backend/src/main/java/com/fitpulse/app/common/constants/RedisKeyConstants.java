package com.fitpulse.app.common.constants;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    private static final String PREFIX = "fitpulse:";

    public static final String LOGIN_CODE = PREFIX + "login:code:%s";
    public static final String REFRESH_TOKEN = PREFIX + "auth:refresh:%s";

    public static String buildKey(String... segments) {
        return PREFIX + String.join(":", segments);
    }
}
