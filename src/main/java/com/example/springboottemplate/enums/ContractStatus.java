package com.example.springboottemplate.enums;

import lombok.Getter;

@Getter
public enum ContractStatus {
    DRAFT(0, "草稿"),
    PENDING_SIGN(1, "待签署"),
    EFFECTIVE(2, "已生效"),
    COMPLETE(3, "已完成"),
    TERMINATED(4, "已终止");

    private final int code;
    private final String desc;

    ContractStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据code获取枚举实例（可选）
    public static ContractStatus of(Integer code) {
        if (code == null) return null;
        for (ContractStatus status : values()) {
            if (status.getCode() == code) return status;
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }
}
