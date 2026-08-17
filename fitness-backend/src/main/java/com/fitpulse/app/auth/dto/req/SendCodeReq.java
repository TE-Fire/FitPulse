package com.fitpulse.app.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送登录验证码请求。
 */
@Data
public class SendCodeReq {

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = ".+@qq\\.com$", message = "仅支持 QQ 邮箱")
    private String email;
}
