package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.service.MinioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequestMapping("/file")
@Api(tags = "文件处理", description = "文件上传下载相关接口")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation(value = "文件上传", notes = "上传文件到MinIO")
    public Response upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径序列");
        }
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID() + fileExtension;

        // 上传到 MinIO
        minioService.upload(file, objectName);

        // 返回桶路径：xy-community/xxx.jpg（也可配置返回完整URL）
        if (minioService.isReturnFullUrl()) {
            return Response.success(minioService.buildPublicUrl(objectName));
        }
        return Response.success(minioService.buildObjectPath(objectName));
    }
}
