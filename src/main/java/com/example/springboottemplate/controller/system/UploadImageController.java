package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.service.MinioService;
import io.minio.StatObjectResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 兼容原本地上传路径 /upload-images/**，改为从 MinIO 读取
 */
@Controller
@RequestMapping("/upload-images")
public class UploadImageController {

    private final MinioService minioService;

    public UploadImageController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String fileName) throws Exception {
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径序列");
        }
        // 前端可能存的是 /xxx.jpg，这里兼容去掉前导斜杠
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        StatObjectResponse stat = minioService.statObject(fileName);
        InputStream inputStream = minioService.getObject(fileName);
        String contentType = stat.contentType() != null ? stat.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(stat.size())
                .body(new InputStreamResource(inputStream));
    }
}
