package com.fitpulse.app.file.service;

import com.fitpulse.app.entity.FileResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务抽象接口。
 * <p>【设计模式：策略模式】
 * <p>将"文件存储"这一行为抽象为接口，不同实现代表不同的存储策略：
 * <ul>
 *   <li>{@link com.fitpulse.app.file.service.impl.LocalFileStorageServiceImpl} - 本地磁盘存储（个人版降级方案）</li>
 *   <li>MinioFileStorageServiceImpl - MinIO 对象存储（未来扩展，本阶段不实现）</li>
 * </ul>
 * Controller/Service 面向此接口编程，通过配置切换具体实现，无需改动业务代码。
 * <p>这符合"开闭原则"——新增存储方式（如 OSS/COS）只需新增实现类，不修改现有代码。
 */
public interface FileStorageService {

    /**
     * 存储文件并返回文件资源记录（含可访问 URL）。
     *
     * @param file     上传的文件
     * @param bucket   存储桶分类（avatar / exercise / food / general）
     * @param userId   上传者用户 ID
     * @return 文件资源记录（已填充 objectKey / fileUrl / storageType 等字段，未持久化到 DB）
     */
    FileResource store(MultipartFile file, String bucket, Long userId);
}
