package com.fitpulse.app.file.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应 VO。
 * <p>对应接口：POST /api/v1/file/upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {

    /** 文件资源 ID（供业务表保存关联） */
    private Long id;

    /** 可直接访问 URL（本地降级 = /files/<bucket>/...） */
    private String fileUrl;
}
