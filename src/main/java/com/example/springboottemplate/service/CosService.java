package com.example.springboottemplate.service;

import com.example.springboottemplate.config.CosProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class CosService {

    private final COSClient cosClient;
    private final CosProperties cosProperties;

    public CosService(COSClient cosClient, CosProperties cosProperties) {
        this.cosClient = cosClient;
        this.cosProperties = cosProperties;
    }

    public String upload(MultipartFile file, String objectName) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StringUtils.hasText(file.getContentType())) {
            metadata.setContentType(file.getContentType());
        }
        try (InputStream inputStream = file.getInputStream()) {
            cosClient.putObject(new PutObjectRequest(
                    cosProperties.getBucket(),
                    toKey(objectName),
                    inputStream,
                    metadata));
        }
        return objectName;
    }

    public InputStream getObject(String objectName) {
        COSObject object = cosClient.getObject(cosProperties.getBucket(), toKey(objectName));
        return object.getObjectContent();
    }

    public ObjectMetadata statObject(String objectName) {
        return cosClient.getObjectMetadata(cosProperties.getBucket(), toKey(objectName));
    }

    public String buildPublicUrl(String objectName) {
        String base = cosProperties.getPublicUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + buildObjectPath(objectName);
    }

    public String buildObjectPath(String objectName) {
        return toKey(objectName);
    }

    public boolean isReturnFullUrl() {
        return cosProperties.isReturnFullUrl();
    }

    private String toKey(String objectName) {
        String name = objectName == null ? "" : objectName.replace("\\", "/");
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        String prefix = cosProperties.getKeyPrefix();
        if (!StringUtils.hasText(prefix)) {
            return name;
        }
        if (name.equals(prefix) || name.startsWith(prefix + "/")) {
            return name;
        }
        return prefix + "/" + name;
    }
}
