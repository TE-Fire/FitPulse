package com.fitpulse.app.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：静态资源映射。
 * <p>将本地磁盘存储的文件目录映射到 URL 路径 /files/**，
 * 使前端通过 http://localhost:8080/files/<bucket>/yyyy/MM/dd/xxx.jpg 直接访问上传的文件。
 * <p>【设计技巧】
 * <p>使用 @Configuration + WebMvcConfigurer 而非 @WebFilter/@Controller，
 * 因为这是 Spring 官方推荐的静态资源配置方式，且不进入 DispatcherServlet 的控制器扫描链，
 * 性能最优（由 ResourceHttpRequestHandler 直接处理，零反射开销）。
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
        // file: 前缀表示文件系统绝对路径，结尾必须带 /
        String base = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        String location = "file:" + base;

        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
