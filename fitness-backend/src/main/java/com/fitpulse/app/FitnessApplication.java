package com.fitpulse.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
@MapperScan("com.fitpulse.app.mapper")
public class FitnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
    }
}
