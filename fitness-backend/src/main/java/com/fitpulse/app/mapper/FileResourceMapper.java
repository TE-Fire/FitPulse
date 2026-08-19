package com.fitpulse.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitpulse.app.entity.FileResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件资源 Mapper（对应 file_resource 表）。
 */
@Mapper
public interface FileResourceMapper extends BaseMapper<FileResource> {
}
