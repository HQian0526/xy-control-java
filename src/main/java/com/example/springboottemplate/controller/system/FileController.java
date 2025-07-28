package com.example.springboottemplate.controller.system;

import com.example.springboottemplate.dto.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;      // 替代 File 的路径抽象
import java.nio.file.Paths;     // 路径工具类
import java.nio.file.Files;     // 文件操作工具类
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/file")
@Api(tags = "文件处理", description = "文件上传下载相关接口")
public class FileController {
    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation(value = "文件上传", notes = "文件上传")
    public Response upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 安全校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        // 生成唯一文件名（防止冲突）
        String originalFilename = file.getOriginalFilename();
        // 在保存文件前检查文件名(防止路径穿越攻击)
        if (originalFilename != null && originalFilename.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径序列");
        }
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // 2. 创建目标路径（确保目录存在）
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); // 自动创建所有父目录
        }

        // 3. 保存文件（NIO.2的Files.copy性能更好）
        Path targetPath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 4. 返回可访问的URL（如：http://localhost:8080/uploaded-images/xxx.jpg）
        return Response.success("/upload-images/" + uniqueFileName);
    }

}
