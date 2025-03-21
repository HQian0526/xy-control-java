package com.example.springboottemplate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // 生成全参构造函数
@NoArgsConstructor // 生成无参构造函数
@ApiModel(description = "桌台信息")
public class Desk {
    @ApiModelProperty(value = "id", required = true)
    private int id;

    @ApiModelProperty(value = "桌台/座位id", required = true)
    private int seatId;

    @ApiModelProperty(value = "桌台/座位名", required = true)
    private String seatName;

    @ApiModelProperty(value = "所属店铺id", required = true)
    private int storeId;

    @ApiModelProperty(value = "备注", required = false)
    private String remark;
}
