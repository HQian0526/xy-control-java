package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.service.CosService;
import com.qcloud.cos.model.ObjectMetadata;
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
 * 兼容原本地上传路径 /upload-images/**，改为从 COS 读取
 */
@Controller
@RequestMapping("/upload-images")
public class UploadImageController {

    private final CosService cosService;

    public UploadImageController(CosService cosService) {
        this.cosService = cosService;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String fileName) throws Exception {
        if (fileName.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径序列");
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        ObjectMetadata stat = cosService.statObject(fileName);
        InputStream inputStream = cosService.getObject(fileName);
        String contentType = stat.getContentType() != null
                ? stat.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(stat.getContentLength())
                .body(new InputStreamResource(inputStream));
    }
}
