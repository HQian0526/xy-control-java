package com.example.springboottemplate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺营业时间。days：1=周一 … 7=周日；start/end 为当天 HH:mm，不跨天。
 * storeId 仅请求时使用（管理员指定店铺），不会写入 business_hours。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreBusinessHours {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long storeId;

    private List<StoreBusinessHoursRule> rules = new ArrayList<>();

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<StoreBusinessHoursRule> getRules() {
        return rules;
    }

    public void setRules(List<StoreBusinessHoursRule> rules) {
        this.rules = rules;
    }
}
