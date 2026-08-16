package com.fitpulse.app.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "fitpulse.ai")
public class AiPromptProperties {
    private String planPromptTemplate;
    private String dietPromptTemplate;
}
