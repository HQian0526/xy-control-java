package com.example.springboottemplate.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnedStoreVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;
    private String storeName;
    private Boolean expired;
    private Integer enablePay;
    private Integer shopStatus;
}
