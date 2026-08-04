package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WxProperties {
    /** 小程序 AppID */
    private String appId;
    /** 小程序 AppSecret */
    private String appSecret;
}
