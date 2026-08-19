package com.fitpulse.app.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件资源实体（对应 file_resource 表）。
 * <p>记录每次上传的文件元数据，供业务表（如 user_profile.avatar_url）关联使用。
 */
@Data
@TableName("file_resource")
public class FileResource {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 上传者用户 ID */
    private Long userId;

    /** 存储桶分类：avatar / exercise / food / general */
    private String bucket;

    /** 对象 key（MinIO 为 object key；本地为相对路径 yyyy/MM/dd/uuid.ext） */
    private String objectKey;

    /** 原始文件名（用户上传时的文件名） */
    private String originalName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 内容类型（MIME，如 image/jpeg） */
    private String contentType;

    /** 可访问 URL（本地降级 = /files/<bucket>/yyyy/MM/dd/uuid.ext） */
    private String fileUrl;

    /** 存储类型 1=MinIO 2=本地文件降级 */
    private Integer storageType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
