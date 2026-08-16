package com.fitpulse.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FitPulse 单体应用启动入口
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.fitpulse.app.*.mapper")
public class FitnessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
        System.out.println("\n========== FitPulse 后端启动成功 http://localhost:8080 ==========\n");
    }
}
