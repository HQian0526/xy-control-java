package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("mall_order")
@ApiModel(description = "商城订单")
public class MallOrder {
    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "店铺id")
    private Long storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "店铺名")
    private String storeName;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "下单用户id")
    private Long userId;

    @ApiModelProperty(value = "付款人openid")
    private String openid;

    @ApiModelProperty(value = "联系电话")
    private String contact;

    @ApiModelProperty(value = "收货地址")
    private String address;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商品金额（元）")
    private BigDecimal goodsAmount;

    @ApiModelProperty(value = "配送费（元）")
    private BigDecimal deliveryFee;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "满减活动id")
    private Long promoId;

    @ApiModelProperty(value = "满减优惠（元）")
    private BigDecimal promoDiscount;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "使用的用户券id")
    private Long userCouponId;

    @ApiModelProperty(value = "优惠券优惠（元）")
    private BigDecimal couponDiscount;

    @ApiModelProperty(value = "优惠合计（元）")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠说明")
    private String discountDesc;

    @ApiModelProperty(value = "应付金额（元）")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "应付金额（分）")
    private Integer payAmountFen;

    /** 0待支付 1已支付 2已关闭 3退款中 4部分退款 5已全额退款 */
    @ApiModelProperty(value = "支付状态")
    private Integer payStatus;

    @ApiModelProperty(value = "微信预支付id")
    private String prepayId;

    @ApiModelProperty(value = "微信支付单号")
    private String transactionId;

    @ApiModelProperty(value = "支付成功时间")
    private Date paidTime;

    @ApiModelProperty(value = "累计已退款金额（元）")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "累计已退款金额（分）")
    private Integer refundAmountFen;

    @ApiModelProperty(value = "处理中的本次退款金额（分）")
    private Integer pendingRefundFen;

    @ApiModelProperty(value = "商户退款单号（最近一次）")
    private String outRefundNo;

    @ApiModelProperty(value = "微信退款单号（最近一次）")
    private String wxRefundId;

    @ApiModelProperty(value = "退款原因（最近一次）")
    private String refundReason;

    @ApiModelProperty(value = "退款操作人")
    private String refundBy;

    @ApiModelProperty(value = "最近一次退款成功时间")
    private Date refundTime;

    /** 0未同步 1成功 2失败 */
    @ApiModelProperty(value = "微信发货同步状态")
    private Integer wxShippingStatus;

    @ApiModelProperty(value = "微信发货错误码")
    private Integer wxShippingErrcode;

    @ApiModelProperty(value = "微信发货错误信息")
    private String wxShippingErrmsg;

    @ApiModelProperty(value = "微信发货重试次数")
    private Integer wxShippingRetry;

    @ApiModelProperty(value = "微信发货同步时间")
    private Date wxShippingTime;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单明细")
    private List<MallOrderItem> items;

    @JsonIgnore
    @TableField(exist = false)
    @ApiModelProperty(value = "支付状态列表（多状态查询）")
    private List<Integer> payStatuses;
}
