package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.MallOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {
    void batchAdd(@Param("list") List<MallOrderItem> list);

    List<MallOrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
