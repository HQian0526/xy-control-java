package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cos")
public class CosProperties {
    /** 腾讯云 API 密钥 SecretId */
    private String secretId;
    /** 腾讯云 API 密钥 SecretKey */
    private String secretKey;
    /** 地域，如 ap-guangzhou */
    private String region;
    /** 存储桶全名，如 xy-community-1304489080 */
    private String bucket;
    /** 对象键前缀，需与线上 URL 路径一致，如 xy-community */
    private String keyPrefix = "xy-community";
    /** 对外访问地址，如 https://file.gzstarfly.com */
    private String publicUrl;
    /** true=返回完整 URL；false=返回 xy-community/xxx */
    private boolean returnFullUrl = false;
}
