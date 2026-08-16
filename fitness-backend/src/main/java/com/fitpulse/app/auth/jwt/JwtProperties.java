package com.fitpulse.app.auth.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "fitpulse.jwt")
public class JwtProperties {
    private String secret;
    private Long accessExpireMinutes = 10080L;
    private Long refreshExpireMinutes = 43200L;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
