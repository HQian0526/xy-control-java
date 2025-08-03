package com.example.springboottemplate.dto.contract;

import lombok.Data;

import java.util.Date;

@Data
public class ContractQueryDTO {
    // 分页参数
    private Integer pageNum = 1;    // 当前页码，默认第1页
    private Integer pageSize = 10;  // 每页条数，默认10条

    // 合同筛选条件
    private String contractNo;      // 合同编号（模糊查询）
    private String contractName;    // 合同名称（模糊查询）
    private Integer contractType;   // 合同类型（1:租赁合同, 2:销售合同...）
    private Integer contractStatus;         // 合同状态（1:待签署, 2:已生效...）
    private Date startDateBegin;    // 合同开始日期范围-起始
    private Date startDateEnd;      // 合同开始日期范围-结束
    private Date signDateBegin;     // 签署日期范围-起始
    private Date signDateEnd;       // 签署日期范围-结束
}
