package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignerVO {
    private Long userId;
    private String name;
    private String mobile;
    private Integer signOrder;     // 签署顺序
    private Integer signStatus;   // 个人签署状态
    private LocalDateTime signTime;
    private String certificateUrl; // 签名证书URL
}
