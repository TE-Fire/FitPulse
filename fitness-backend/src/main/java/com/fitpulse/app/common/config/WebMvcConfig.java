package com.fitpulse.app.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置：静态资源映射。
 * <p>将本地磁盘存储的文件目录映射到 URL 路径 /files/**，
 * 使前端通过 http://localhost:8080/files/<bucket>/yyyy/MM/dd/xxx.jpg 直接访问上传的文件。
 * <p>【设计技巧】
 * <p>使用 @Configuration + WebMvcConfigurer 而非 @WebFilter/@Controller，
 * 因为这是 Spring 官方推荐的静态资源配置方式，且不进入 DispatcherServlet 的控制器扫描链，
 * 性能最优（由 ResourceHttpRequestHandler 直接处理，零反射开销）。
 * <p>【Windows 路径坑】
 * <p>不能用 "file:" + path 拼接 URL（如 file:D:/FitPulseData/files/），
 * 因为 Java 会把 "D" 解析为 host，"/FitPulseData/files/" 解析为 path，导致找不到文件。
 * <p>正确做法：使用 PathResource 直接包装 java.nio.file.Path，彻底绕开 URL 解析。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** 本地文件存储根路径（与 LocalFileStorageServiceImpl 共用配置项） */
    @Value("${fitpulse.storage.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /files/** URL 映射到本地磁盘目录
        // 【关键】使用 PathResource 而非 "file:" URL，避免 Windows 下盘符被误解析为 host
        Path path = Paths.get(uploadPath);
        registry.addResourceHandler("/files/**")
                .addResourceLocations(new PathResource(path));
    }
}
