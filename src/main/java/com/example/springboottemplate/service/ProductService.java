package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ProductService {
    Response addProduct(Product product, HttpServletRequest request);

    Response findProduct(Product product, Integer pageNum, Integer pageSize);

    Response updateProduct(Product product, HttpServletRequest request);

    Response deleteProduct(List<Integer> idList);
}
