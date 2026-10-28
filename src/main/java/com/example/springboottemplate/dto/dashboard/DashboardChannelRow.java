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
public class DashboardChannelRow {
    private Integer channel;
    private String channelName;
    private BigDecimal amount;
    private Integer orderCount;
}
