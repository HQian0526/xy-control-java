package com.example.springboottemplate.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("product")
@ApiModel(description = "商品信息")
public class Product {
    @ApiModelProperty(value = "主键id", required = true)
    private Long id;

    @ApiModelProperty(value = "商品id（后端自动生成）", required = false)
    private Long productId;

    @ApiModelProperty(value = "分类id", required = false)
    private Long catagoryId;

    @TableField(exist = false)
    @ApiModelProperty(value = "分类名称", required = false)
    private String catagoryName;

    @ApiModelProperty(value = "所属商户", required = false)
    private String storeId;

    @TableField(exist = false)
    @ApiModelProperty(value = "所属商户名", required = false)
    private String storeName;

    @ApiModelProperty(value = "商品名称", required = false)
    private String productName;

    @ApiModelProperty(value = "商品图片", required = false)
    private String productImg;

    @ApiModelProperty(value = "价格", required = false)
    private BigDecimal price;

    @ApiModelProperty(value = "库存", required = false)
    private Integer productNum;

    @ApiModelProperty(value = "已售", required = false)
    private Integer saleNum;

    @ApiModelProperty(value = "描述", required = false)
    private String remark;

    @ApiModelProperty(value = "状态：1上架 0下架", required = false)
    private Integer productStatus;

    @ApiModelProperty(value = "创建人", required = false)
    private String createdBy;

    @ApiModelProperty(value = "创建时间", required = false)
    private Date createdTime;

    @ApiModelProperty(value = "更新人", required = false)
    private String updateBy;

    @ApiModelProperty(value = "更新时间", required = false)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除标识", required = false)
    @TableLogic
    private Integer deleted;
}
