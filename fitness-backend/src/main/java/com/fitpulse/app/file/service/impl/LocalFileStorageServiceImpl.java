package com.fitpulse.app.file.service.impl;

import com.fitpulse.app.entity.FileResource;
import com.fitpulse.app.file.enums.FileErrorCode;
import com.fitpulse.app.file.service.FileStorageService;
import com.fitpulse.app.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现（策略模式 - 具体策略）。
 * <p>个人版降级方案：无需安装 MinIO，文件写入本地磁盘 + Spring MVC 静态映射访问。
 * <p>通过配置 {@code fitpulse.storage.type=local} 激活（@ConditionalOnProperty）。
 * <p>文件路径：{upload-path}/{bucket}/{yyyy/MM/dd}/{uuid}.{ext}
 * <p>访问 URL：/files/{bucket}/{yyyy/MM/dd}/{uuid}.{ext}（由 WebMvcConfig 静态映射）
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    /** 本地存储类型标识（file_resource.storage_type = 2） */
    private static final Integer STORAGE_TYPE_LOCAL = 2;

    /** 允许的文件扩展名白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    /** 允许的 bucket 白名单 */
    private static final Set<String> ALLOWED_BUCKETS = Set.of(
            "avatar", "exercise", "food", "general"
    );

    /** URL 路径前缀（与 WebMvcConfig 静态映射一致） */
    private static final String URL_PREFIX = "/files/";

    /** 日期目录格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${fitpulse.storage.upload-path}")
    private String uploadPath;

    @Override
    public FileResource store(MultipartFile file, String bucket, Long userId) {
        // 1. 基础校验
        validateFile(file);
        validateBucket(bucket);

        // 2. 解析文件扩展名
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);

        // 3. 构造相对路径：{bucket}/{yyyy/MM/dd}/{uuid}.{ext}
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = bucket + "/" + dateDir + "/" + fileName;

        // 4. 写入本地磁盘
        Path targetPath = Paths.get(uploadPath, relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            log.error("[文件存储失败] userId={}, bucket={}, fileName={}", userId, bucket, fileName, e);
            throw new BusinessException(FileErrorCode.FILE_STORAGE_FAILED);
        }

        // 5. 构造可访问 URL
        String fileUrl = URL_PREFIX + relativePath;

        log.info("[文件存储成功] userId={}, bucket={}, size={}, url={}",
                userId, bucket, file.getSize(), fileUrl);

        // 6. 返回文件资源记录（未持久化，由调用方写 DB）
        FileResource fileResource = new FileResource();
        fileResource.setUserId(userId);
        fileResource.setBucket(bucket);
        fileResource.setObjectKey(relativePath);
        fileResource.setOriginalName(originalName);
        fileResource.setFileSize(file.getSize());
        fileResource.setContentType(file.getContentType());
        fileResource.setFileUrl(fileUrl);
        fileResource.setStorageType(STORAGE_TYPE_LOCAL);
        return fileResource;
    }

    /**
     * 基础校验：文件非空、有内容。
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }
    }

    /**
     * bucket 白名单校验，防止路径穿越攻击。
     */
    private void validateBucket(String bucket) {
        if (bucket == null || !ALLOWED_BUCKETS.contains(bucket)) {
            throw new BusinessException(FileErrorCode.BUCKET_INVALID);
        }
    }

    /**
     * 提取扩展名并校验白名单。
     */
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
