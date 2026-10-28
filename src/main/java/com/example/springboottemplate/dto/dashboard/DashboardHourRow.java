package com.example.springboottemplate.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardHourRow {
    private String hour;
    private BigDecimal sales;
    private Integer orderCount;
}
