package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
@Api(tags = "商品管理", description = "商品相关接口")
public class ProductController {
    @Autowired
    private ProductService productService;

    // 新增商品
    @PostMapping("/addProduct")
    @ResponseBody
    @ApiOperation(value = "添加商品", notes = "传入商品各项信息进行添加")
    public Response addProduct(@RequestBody Product product, HttpServletRequest request){
        return productService.addProduct(product, request);
    }

    //查询所有商品
    @GetMapping("/findProduct")
    @ResponseBody
    @ApiOperation(value = "查询所有商品", notes = "查询商品表中所有商品")
    public Response findProduct(Product product, Integer pageNum, Integer pageSize){
        return productService.findProduct(product, pageNum, pageSize);
    }

    //修改商品信息
    @PutMapping("/updateProduct")
    @ResponseBody
    @ApiOperation(value = "修改商品信息", notes = "根据id更新商品信息")
    public Response updateProduct(@RequestBody Product product, HttpServletRequest request){
        return productService.updateProduct(product, request);
    }

    //删除商品信息
    @DeleteMapping("/deleteProduct")
    @ResponseBody
    @ApiOperation(value = "删除商品", notes = "根据id删除商品")
    public Response deleteProduct(@RequestBody List<Integer> idList){
        return productService.deleteProduct(idList);
    }
}
