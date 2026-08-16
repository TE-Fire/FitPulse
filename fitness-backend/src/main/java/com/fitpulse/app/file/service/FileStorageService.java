package com.fitpulse.app.file.service;

import com.fitpulse.app.common.config.MinioConfig;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.result.ResultCode;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final MinioClient minioClient;
    private final MinioConfig cfg;

    private void ensureBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(cfg.getBucket()).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(cfg.getBucket()).build());
                // 个人用直接 public read，方便访问
                String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}
                      ]
                    }
                    """.formatted(cfg.getBucket());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(cfg.getBucket()).config(policy).build());
            }
        } catch (Exception e) {
            log.error("MinIO Bucket 初始化失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL, "MinIO连接失败");
        }
    }

    /**
     * 上传文件，返回访问 URL
     * @param file 上传的文件
     * @param bizType 业务类型 avatar/exercise/food/meal
     */
    public UploadResult upload(MultipartFile file, String bizType) {
        ensureBucket();
        if (file == null || file.isEmpty()) throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件为空");
        try {
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf("."));
            String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String objectName = (bizType == null ? "misc" : bizType) + "/" + dateDir + "/"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + ext;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            String url = cfg.getPublicBaseUrl() + "/" + objectName;
            return new UploadResult(objectName, url, original, file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("上传失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAIL);
        }
    }

    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(cfg.getBucket()).object(objectName).build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(cfg.getBucket()).object(objectName).build());
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            throw new BusinessException(ResultCode.FILE_DELETE_FAIL);
        }
    }

    public record UploadResult(String objectName, String url, String originalName, long size, String contentType) {}
}
