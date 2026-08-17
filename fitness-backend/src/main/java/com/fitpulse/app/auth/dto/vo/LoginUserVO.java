package com.fitpulse.app.auth.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/刷新 成功返回。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserVO {

    private String accessToken;

    private String refreshToken;

    private Long userId;

    private String username;
}
