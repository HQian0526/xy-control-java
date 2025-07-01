package com.example.springboottemplate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuRouteDto {
    private String path;
    private String componentPath;
    private String name;
    private String component;
    private String redirect;
    private Meta meta;
    private List<MenuRouteDto> children;

    @Data
    public static class Meta {
        private String title;
        private String icon;
        private Boolean ignoreKeepAlive;
        private Boolean hideMenu;
        private Boolean hideBreadcrumb;
        private Boolean hideTab;
        private Boolean carryParam;
        private Boolean hideChildrenInMenu;
        private String currentActiveMenu;
        private Boolean ignoreRoute;
    }
}
