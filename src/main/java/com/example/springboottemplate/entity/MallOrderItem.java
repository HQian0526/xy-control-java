package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@TableName("mall_order_item")
@ApiModel(description = "商城订单明细")
public class MallOrderItem {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单主键id")
    private Long orderId;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "商品业务id")
    private Long productId;

    @ApiModelProperty(value = "商品名称快照")
    private String productName;

    @ApiModelProperty(value = "商品图片快照")
    private String productImg;

    @ApiModelProperty(value = "单价（元）")
    private BigDecimal price;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "小计（元）")
    private BigDecimal amount;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;
}
