package com.fitpulse.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求。
 * <p>type=1（密码登录）：password 必填；type=2（验证码登录）：code 必填（6位数字）。
 */
@Data
public class LoginReq {

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱登录")
    private String email;

    @NotNull(message = "登录类型不能为空")
    private Integer type;

    /** type=1（密码登录）时必填 */
    @Size(min = 8, max = 64, message = "密码长度需 8-64 位")
    private String password;

    /** type=2（验证码登录）时必填，6 位数字 */
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String code;
}
