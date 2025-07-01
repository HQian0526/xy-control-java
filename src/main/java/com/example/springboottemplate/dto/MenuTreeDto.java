package com.example.springboottemplate.dto;
import lombok.Data;

import java.util.List;

@Data
public class MenuTreeDto {
    private Long id;
    private String menuName;
    private String menuCode;
    private Long parentId;
    private Integer menuType;
    private String iconUrl;
    private Integer sort;
    private Integer level;
    private String path;
    private String componentPath;
    private List<MenuTreeDto> children;
}
