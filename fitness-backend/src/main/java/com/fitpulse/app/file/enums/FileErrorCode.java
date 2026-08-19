package com.fitpulse.app.file.enums;

import com.fitpulse.app.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * File 模块专属业务错误枚举。
 * <p>每个枚举对应 file 模块内一个精确业务场景，实现 {@link BaseExceptionInterface}
 * 可直接作为 BusinessException 构造参数。
 */
@Getter
@AllArgsConstructor
public enum FileErrorCode implements BaseExceptionInterface {

    // ========== 400 参数类 ==========
    FILE_EMPTY(400, "文件不能为空"),
    FILE_TOO_LARGE(400, "文件大小超过限制"),
    FILE_EXTENSION_INVALID(400, "文件格式不支持，仅允许 jpg/jpeg/png/webp/gif"),
    BUCKET_INVALID(400, "存储桶分类非法"),

    // ========== 500 服务端异常 ==========
    FILE_STORAGE_FAILED(500, "文件存储失败，请稍后重试");

    private final Integer errorCode;
    private final String errorMessage;

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
