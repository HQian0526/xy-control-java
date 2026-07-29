package com.example.springboottemplate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /** MinIO服务地址，如 http://127.0.0.1:9000 */
    private String endpoint;
    /** 访问密钥 */
    private String accessKey;
    /** 密钥 */
    private String secretKey;
    /** 桶名称 */
    private String bucket;
    /** 对外访问地址（可与endpoint相同，或走nginx域名） */
    private String publicUrl;
    /** 是否返回完整MinIO URL；false时返回 bucket/object 相对路径 */
    private boolean returnFullUrl = false;
}
