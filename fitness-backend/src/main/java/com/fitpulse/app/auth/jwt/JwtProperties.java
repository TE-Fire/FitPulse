package com.fitpulse.app.auth.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fitpulse.jwt")
public class JwtProperties {

    /** HS256 签名密钥（需 ≥ 256 bits） */
    private String secret;

    /** accessToken 过期时间（分钟），默认 24 小时 */
    private long accessExpireMinutes = 1440;

    /** refreshToken 过期时间（分钟），默认 30 天 */
    private long refreshExpireMinutes = 43200;

    /** 请求头名称 */
    private String header = "Authorization";

    /** Token 前缀 */
    private String prefix = "Bearer ";
}
