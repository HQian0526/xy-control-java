package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("store_balance_adjust")
public class StoreBalanceAdjust {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long storeId;
    private BigDecimal amount;
    private String bizNo;
    private String orderNo;
    private String remark;
    private Integer settleStatus;
    private Date settleTime;
    private String settleBy;
    private Date createdTime;
    @TableLogic
    private Integer deleted;
}
