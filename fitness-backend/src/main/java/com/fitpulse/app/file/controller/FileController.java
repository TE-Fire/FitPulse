package com.fitpulse.app.file.controller;

import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileService;

    @PostMapping("/upload")
    public Result<FileStorageService.UploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false, defaultValue = "misc") String bizType) {
        return Result.success(fileService.upload(file, bizType));
    }

    @GetMapping("/list")
    public Result<List<Object>> list() { return Result.success(List.of()); }

    @DeleteMapping("/{objectName}")
    public Result<Void> delete(@PathVariable String objectName) {
        fileService.delete(objectName);
        return Result.success();
    }
}
