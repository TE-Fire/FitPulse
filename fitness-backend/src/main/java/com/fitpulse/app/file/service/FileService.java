package com.fitpulse.app.file.service;

import com.fitpulse.app.file.dto.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * File 业务接口：文件上传。
 * <p>Controller 面向此接口注入，Spring 自动装配 impl 包下的实现类。
 */
public interface FileService {

    /**
     * 上传文件到存储（本地降级或 MinIO），并写入 file_resource 表。
     *
     * @param file   上传的文件
     * @param bucket 存储桶分类（avatar / exercise / food / general）
     * @param userId 上传者用户 ID
     * @return 文件上传响应（含 ID 和可访问 URL）
     */
    FileUploadVO upload(MultipartFile file, String bucket, Long userId);
}
