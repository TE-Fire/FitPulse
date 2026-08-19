package com.fitpulse.app.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置参数。
 * <p>对应 application.yml 中 {@code fitpulse.minio.*} 配置段。
 * <p>仅在 {@code fitpulse.storage.type=minio} 时生效（由 MinioConfig 的 @ConditionalOnProperty 控制）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "fitpulse.minio")
public class MinioProperties {

    /** MinIO 服务地址（含端口，不含 bucket） */
    private String endpoint;

    /** 访问密钥 */
    private String accessKey;

    /** 秘密密钥 */
    private String secretKey;

    /** 默认 bucket 名称（文件存储桶） */
    private String bucket;

    /** 公开访问基础 URL（拼接 objectKey 后即文件可访问 URL） */
    private String publicBaseUrl;
}
