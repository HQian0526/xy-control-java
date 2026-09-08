package com.example.springboottemplate.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺营业时间。days：1=周一 … 7=周日；start/end 为当天 HH:mm，不跨天。
 */
public class StoreBusinessHours {
    private List<StoreBusinessHoursRule> rules = new ArrayList<>();

    public List<StoreBusinessHoursRule> getRules() {
        return rules;
    }

    public void setRules(List<StoreBusinessHoursRule> rules) {
        this.rules = rules;
    }
}
