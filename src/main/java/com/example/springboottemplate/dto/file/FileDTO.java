package com.example.springboottemplate.dto.file;

import lombok.Data;

@Data
public class FileDTO {
    private Long uid;
    private String fileName;
    private String filePath;
    private String fileSize;
}
