package com.fitpulse.app.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fitpulse.mail")
public class MailProperties {

    private String smtpHost = "smtp.qq.com";
    private int smtpPort = 465;
    private String username;
    private String password;
    private String encoding = "UTF-8";
    private int timeout = 5000;
    private String senderName = "FitPulse";

    public boolean isConfigured() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}
