package com.fitpulse.app.file.service.impl;

import com.fitpulse.app.entity.FileResource;
import com.fitpulse.app.file.dto.vo.FileUploadVO;
import com.fitpulse.app.file.service.FileService;
import com.fitpulse.app.file.service.FileStorageService;
import com.fitpulse.app.mapper.FileResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileService 默认实现。
 * <p>【设计模式：门面模式思想】
 * <p>FileService 作为业务门面，内部委托 FileStorageService 完成实际存储，
 * 自己负责"存储 + 写 DB"的组合事务，对 Controller 屏蔽底层存储细节。
 * <p>编码风格对齐 auth/user 模块：@Slf4j + @Service + @RequiredArgsConstructor。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageService fileStorageService;
    private final FileResourceMapper fileResourceMapper;

    @Override
    public FileUploadVO upload(MultipartFile file, String bucket, Long userId) {
        // 1. 委托存储策略完成文件落盘（本地或 MinIO）
        FileResource fileResource = fileStorageService.store(file, bucket, userId);

        // 2. 写入 file_resource 表
        fileResourceMapper.insert(fileResource);

        log.info("[文件上传完成] fileId={}, userId={}, bucket={}",
                fileResource.getId(), userId, bucket);

        // 3. 返回 VO
        return FileUploadVO.builder()
                .id(fileResource.getId())
                .fileUrl(fileResource.getFileUrl())
                .build();
    }
}
