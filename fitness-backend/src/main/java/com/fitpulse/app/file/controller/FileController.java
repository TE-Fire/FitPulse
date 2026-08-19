package com.fitpulse.app.file.controller;

import com.fitpulse.app.auth.jwt.CurrentUser;
import com.fitpulse.app.common.annotation.RequestLog;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.file.dto.vo.FileUploadVO;
import com.fitpulse.app.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * File 模块入口。
 * <p>所有接口均需认证（SecurityConfig 中 anyRequest().authenticated()）。
 * <p>本次范围：通用文件上传接口（POST /file/upload）。
 */
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件（通用接口，bucket 区分头像/动作图/饮食照/通用）。
     * <p>Content-Type: multipart/form-data
     *
     * @param file   上传的文件（≤10MB；图片=jpg/png/webp/gif）
     * @param bucket 存储桶分类（avatar / exercise / food / general）
     */
    @PostMapping("/upload")
    @RequestLog("文件上传")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bucket") String bucket) {
        Long userId = CurrentUser.getUserId();
        return Result.success(fileService.upload(file, bucket, userId));
    }
}
