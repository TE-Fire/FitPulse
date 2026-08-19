package com.fitpulse.app.file.service.impl;

import com.fitpulse.app.common.config.MinioProperties;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.entity.FileResource;
import com.fitpulse.app.file.enums.FileErrorCode;
import com.fitpulse.app.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * MinIO 对象存储实现（策略模式 - 具体策略）。
 * <p>通过配置 {@code fitpulse.storage.type=minio} 激活（@ConditionalOnProperty）。
 * <p>对象 key 结构：{bucket}/{yyyy/MM/dd}/{uuid}.{ext}
 * <p>访问 URL：{public-base-url}/{objectKey}（前端可直接访问 MinIO 桶，需配置桶访问策略为 public-read）
 * <p>【编码风格】与 LocalFileStorageServiceImpl 保持一致的校验逻辑和白名单，
 * 但存储动作委托给 MinioClient.putObject()，无需本地磁盘 IO。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fitpulse.storage.type", havingValue = "minio")
public class MinioFileStorageServiceImpl implements FileStorageService {

    /** MinIO 存储类型标识（file_resource.storage_type = 1） */
    private static final Integer STORAGE_TYPE_MINIO = 1;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    private static final Set<String> ALLOWED_BUCKETS = Set.of(
            "avatar", "exercise", "food", "general"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public FileResource store(MultipartFile file, String bucket, Long userId) {
        // 1. 基础校验（与 Local 实现保持一致）
        validateFile(file);
        validateBucket(bucket);

        // 2. 解析文件扩展名
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);

        // 3. 构造对象 key：{bucket}/{yyyy/MM/dd}/{uuid}.{ext}
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String objectKey = bucket + "/" + dateDir + "/" + fileName;

        // 4. 上传到 MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("[MinIO 上传失败] userId={}, bucket={}, objectKey={}", userId, bucket, objectKey, e);
            throw new BusinessException(FileErrorCode.FILE_STORAGE_FAILED);
        }

        // 5. 构造可访问 URL
        String publicBaseUrl = minioProperties.getPublicBaseUrl();
        String fileUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl + objectKey
                : publicBaseUrl + "/" + objectKey;

        log.info("[MinIO 上传成功] userId={}, bucket={}, size={}, url={}",
                userId, bucket, file.getSize(), fileUrl);

        // 6. 返回文件资源记录
        FileResource fileResource = new FileResource();
        fileResource.setUserId(userId);
        fileResource.setBucket(bucket);
        fileResource.setObjectKey(objectKey);
        fileResource.setOriginalName(originalName);
        fileResource.setFileSize(file.getSize());
        fileResource.setContentType(file.getContentType());
        fileResource.setFileUrl(fileUrl);
        fileResource.setStorageType(STORAGE_TYPE_MINIO);
        return fileResource;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
    }

    private void validateBucket(String bucket) {
        if (bucket == null || !ALLOWED_BUCKETS.contains(bucket)) {
            throw new BusinessException(FileErrorCode.BUCKET_INVALID);
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException(FileErrorCode.FILE_EXTENSION_INVALID);
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(FileErrorCode.FILE_EXTENSION_INVALID);
        }
        return extension;
    }
}
