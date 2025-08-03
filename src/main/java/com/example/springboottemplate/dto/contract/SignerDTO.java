package com.example.springboottemplate.dto.contract;

import lombok.Data;

@Data
public class SignerDTO {
    private Long userId;
    private String name;
    private String mobile;
    private String idCard;
    private String email;
    private Integer signOrder;
}
