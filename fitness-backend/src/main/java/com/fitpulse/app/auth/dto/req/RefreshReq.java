package com.fitpulse.app.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求。
 */
@Data
public class RefreshReq {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
