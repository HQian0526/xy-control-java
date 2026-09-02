package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserProperties {
    /** 新增用户未传密码时的初始密码前缀，如 xy@ */
    private String defaultPasswordPrefix = "xy@";
}
