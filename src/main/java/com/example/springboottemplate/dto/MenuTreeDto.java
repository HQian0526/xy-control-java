package com.example.springboottemplate.dto;
import lombok.Data;

import java.util.List;

@Data
public class MenuTreeDto {
    private String id;
    private String menuName;
    private String menuCode;
    private String parentId;
    private Integer menuType;
    private String iconUrl;
    private Integer sort;
    private Integer level;
    private String path;
    private String componentPath;
    private Integer isShow;
    private Boolean checked;
    private List<MenuTreeDto> children;
}
