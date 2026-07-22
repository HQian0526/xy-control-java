package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.entity.Catagory;
import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.CatagoryMapper;
import com.example.springboottemplate.mapper.ProductMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.service.ProductService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductServiceimpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private CatagoryMapper catagoryMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Response addProduct(Product product, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名和用户id
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Integer userId = jwtUtil.getUserId(claims);
        // 3. 校验当前用户是否绑定商户
        Store query = new Store();
        query.setUserId(userId == null ? null : Long.valueOf(userId));
        query.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(query);
        if (ValidateUtil.isEmpty(storeList)) {
            throw new BusinessException("您不是商户类型的用户，暂时无法上架商品/商品分类");
        }
        // 4. 自动绑定商户storeId
        Store store = storeList.get(0);
        if (store.getStoreId() != null) {
            product.setStoreId(String.valueOf(store.getStoreId()));
        }
        // 5. 雪花算法自动生成商品id，默认上架
        product.setProductId(IdWorker.getId());
        if (product.getProductStatus() == null) {
            product.setProductStatus(1);
        }
        if (product.getProductNum() == null) {
            product.setProductNum(0);
        }
        if (product.getSaleNum() == null) {
            product.setSaleNum(0);
        }
        product.setCreatedTime(new Date());
        product.setCreatedBy(username);
        productMapper.addProduct(product);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findProduct(Product product, Integer pageNum, Integer pageSize) {
        // 传了分页参数才开启分页，否则返回全部数据
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        // 查询数据
        List<Product> list = productMapper.findProduct(product);
        // 添加自定义storeName、catagoryName字段
        list.forEach(item -> {
            if (item.getStoreId() != null && !item.getStoreId().isEmpty()) {
                try {
                    Store store = storeMapper.selectByStoreId(Long.parseLong(item.getStoreId()));
                    item.setStoreName(store != null ? store.getStoreName() : null);
                } catch (NumberFormatException e) {
                    item.setStoreName(null);
                }
            }
            if (item.getCatagoryId() != null) {
                Catagory query = new Catagory();
                query.setCatagoryId(item.getCatagoryId());
                query.setDeleted(0);
                List<Catagory> catagoryList = catagoryMapper.findCatagory(query);
                if (!ValidateUtil.isEmpty(catagoryList)) {
                    item.setCatagoryName(catagoryList.get(0).getCatagoryName());
                }
            }
        });
        // 封装分页结果
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());  // 当前页数据
        data.put("total", pageInfo.getTotal()); // 总记录数
        data.put("pages", pageInfo.getPages()); // 总页数
        data.put("pageNum", pageInfo.getPageNum()); // 当前页码
        data.put("pageSize", pageInfo.getPageSize()); // 每页数量
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateProduct(Product product, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        product.setUpdateBy(username);
        product.setUpdateTime(new Date());
        productMapper.updateProduct(product);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteProduct(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        Integer affectedRows = productMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
