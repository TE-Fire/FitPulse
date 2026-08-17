package com.fitpulse.app.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginTypeEnum {

    PASSWORD(1, "密码登录"),
    VERIFY_CODE(2, "验证码登录");

    private final int code;
    private final String desc;

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
