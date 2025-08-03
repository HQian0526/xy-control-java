package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractSignatureVO {
    private Long id;
    private String signRequestId;  // 签署请求ID
    private Integer signStatus;   // 签署状态
    private LocalDateTime signTime;
    private List<SignerVO> signers; // 签署人列表
}
