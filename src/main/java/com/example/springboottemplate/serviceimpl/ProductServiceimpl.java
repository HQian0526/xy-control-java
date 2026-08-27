package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.entity.Catagory;
import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.CatagoryMapper;
import com.example.springboottemplate.mapper.ProductMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
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

import java.util.Collections;
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
    private UserMapper userMapper;
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
        product.setDeleted(0);
        product.setCreatedTime(new Date());
        product.setCreatedBy(username);
        productMapper.addProduct(product);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findProduct(Product product, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        // 1. 解析JWT获取当前用户（游客无 token 时按普通用户 + 请求 storeId 查询）
        Integer userId = jwtUtil.tryGetUserId(request);
        // 2. 查询用户身份：1普通用户 2商户用户 3管理员
        User user = userId == null ? null : userMapper.selectById(userId.longValue());
        Integer identityType = user == null ? null : user.getIdentityType();

        // 普通用户：按请求传入的 storeId 查询对应店铺商品（未传则空）
        if (identityType == null || identityType == 1) {
            if (product.getStoreId() == null || product.getStoreId().isEmpty()) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
        }
        // 商户用户：仅返回本账号绑定商户下的商品
        if (identityType == 2) {
            Store storeQuery = new Store();
            storeQuery.setUserId(Long.valueOf(userId));
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            product.setStoreId(String.valueOf(storeList.get(0).getStoreId()));
        }
        // 管理员(3)：不额外过滤，返回全部

        // 传了分页参数才开启分页，否则返回全部数据
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
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
        return buildPageResponse(list, pageNum, pageSize);
    }

    private Response buildPageResponse(List<Product> list, Integer pageNum, Integer pageSize) {
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
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
    public Response deleteProduct(List<Long> idList) {
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
