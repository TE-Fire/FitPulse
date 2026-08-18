package com.fitpulse.app.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 忘记密码场景：发送密码重置验证码请求。
 * <p>邮箱仅限 @qq.com；60s 内不可重复发送；邮箱必须已注册。
 */
@Data
public class ForgotPasswordSendCodeReq {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱")
    private String email;
}
