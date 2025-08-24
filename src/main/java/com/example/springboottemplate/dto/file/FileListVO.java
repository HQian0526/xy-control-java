package com.example.springboottemplate.dto.file;

import lombok.Data;

import java.util.List;

@Data
public class FileListVO {
    private String fileJson;

    private List<FileDTO> fileList;
}
