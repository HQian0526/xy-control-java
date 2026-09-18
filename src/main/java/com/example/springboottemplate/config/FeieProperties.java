package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "feie")
public class FeieProperties {
    /** 飞鹅开放平台账号（邮箱） */
    private String user = "";
    /** 飞鹅开发者密钥；实名前可留空 */
    private String ukey = "";
    private String apiUrl = "https://api.feieyun.cn/Api/Open/";

    public boolean configured() {
        return StringUtils.hasText(user) && StringUtils.hasText(ukey);
    }
}
