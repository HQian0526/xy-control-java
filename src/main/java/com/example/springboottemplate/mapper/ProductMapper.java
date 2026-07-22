package com.example.springboottemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottemplate.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    void addProduct(Product product);  //新增商品

    List<Product> findProduct(Product product); //查找所有商品

    void updateProduct(Product product); //修改商品信息
}
