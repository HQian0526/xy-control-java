package com.example.springboottemplate.service;

import com.example.springboottemplate.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    /**
     * 上传文件到 MinIO，返回对象名
     */
    public String upload(MultipartFile file, String objectName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build());
        }
        return objectName;
    }

    /**
     * 从 MinIO 读取文件流
     */
    public InputStream getObject(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(objectName)
                .build());
    }

    /**
     * 获取对象元信息
     */
    public StatObjectResponse statObject(String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(objectName)
                .build());
    }

    /**
     * 拼接可访问URL
     */
    public String buildPublicUrl(String objectName) {
        String base = minioProperties.getPublicUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + minioProperties.getBucket() + "/" + objectName;
    }

    /**
     * 拼接桶相对路径：xy-community/xxx.jpg
     */
    public String buildObjectPath(String objectName) {
        return minioProperties.getBucket() + "/" + objectName;
    }

    public boolean isReturnFullUrl() {
        return minioProperties.isReturnFullUrl();
    }
}
