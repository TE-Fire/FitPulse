package com.fitpulse.app.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求。
 * <p>对应接口：PUT /api/v1/user/password
 * <p>服务端 BCrypt 比对 oldPassword，通过后加密 newPassword 更新。
 */
@Data
public class ChangePasswordReq {

    /** 当前密码明文 */
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    /** 新密码（8-64 位，至少含字母+数字） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需 8-64 位")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "密码需同时包含字母和数字"
    )
    private String newPassword;

    /** 确认新密码（必须与 newPassword 一致） */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
