package com.fitpulse.app.common.enums;

import lombok.Getter;

@Getter
public enum LoginTypeEnum {

    PASSWORD(1, "密码登录"),
    VERIFY_CODE(2, "验证码登录");

    private final int code;
    private final String desc;

    LoginTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LoginTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
