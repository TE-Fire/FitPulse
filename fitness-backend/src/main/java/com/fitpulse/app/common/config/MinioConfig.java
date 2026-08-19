package com.fitpulse.app.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置。
 * <p>【设计模式：条件装配】
 * <p>通过 @ConditionalOnProperty 实现"配置驱动的 Bean 装配"：
 * <ul>
 *   <li>当 fitpulse.storage.type=minio 时，本配置类生效，注入 MinioClient Bean</li>
 *   <li>当 fitpulse.storage.type=local 时，本配置类不生效，避免 MinIO 相关 Bean 被创建</li>
 * </ul>
 * <p>这样实现了"零代码切换存储策略"——只改 yml 配置即可，无需修改任何 Java 代码。
 * <p>【Bucket 自动创建】
 * <p>使用 @PostConstruct 在 Bean 初始化完成后检查 bucket 是否存在，不存在则自动创建，
 * 避免首次启动时因 bucket 缺失导致上传失败。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "minio")
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 创建 MinioClient Bean。
     * <p>MinIO SDK 8.x 使用 Builder 模式构建客户端。
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * Bean 初始化后检查并创建 bucket。
     * <p>【设计技巧】容错设计——首次启动自动建桶，避免运维手动操作。
     */
    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            String bucketName = minioProperties.getBucket();

            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("[MinIO] Bucket 不存在，已自动创建：{}", bucketName);
            } else {
                log.info("[MinIO] Bucket 已存在：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("[MinIO] Bucket 初始化失败，请检查 MinIO 服务是否启动", e);
            throw new RuntimeException("MinIO bucket 初始化失败", e);
        }
    }
}
