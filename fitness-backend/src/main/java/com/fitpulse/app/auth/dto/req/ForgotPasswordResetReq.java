package com.fitpulse.app.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码场景：重置密码请求。
 * <p>邮箱 + 验证码 + 新密码 + 确认密码四字段，两次密码必须一致。
 */
@Data
public class ForgotPasswordResetReq {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱")
    private String email;

    @NotBlank(message = "密码重置验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "密码重置验证码应为6位数字")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需 8-64 位")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "密码需同时包含字母和数字"
    )
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
