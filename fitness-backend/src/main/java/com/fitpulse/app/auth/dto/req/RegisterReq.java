package com.fitpulse.app.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 * <p>邮箱仅限 @qq.com；密码 8-64 位，至少包含字母和数字。
 */
@Data
public class RegisterReq {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱注册")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需 8-64 位")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "密码需同时包含字母和数字"
    )
    private String password;
}
