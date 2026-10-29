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
import com.example.springboottemplate.utils.MiniappUiHelper;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.StoreExpireHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private LogicDeleteHelper logicDeleteHelper;

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private CatagoryMapper catagoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StoreAuthHelper storeAuthHelper;

    @Override
    public Response addProduct(Product product, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名和用户id
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Long userId = jwtUtil.getUserId(claims);
        User operator = userId == null ? null : userMapper.selectById(userId);
        Integer identityType = operator == null ? null : operator.getIdentityType();
        // 3. 管理员按请求店铺写入；商户自动绑定本店
        if (identityType != null && identityType == 3) {
            if (product.getStoreId() == null || product.getStoreId().isEmpty()) {
                throw new BusinessException("请选择店铺");
            }
            try {
                Store target = storeMapper.selectByStoreId(Long.parseLong(product.getStoreId()));
                if (target == null) {
                    throw new BusinessException("店铺不存在");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("店铺不存在");
            }
        } else {
            Long requested = storeAuthHelper.parseStoreId(product.getStoreId());
            if (requested == null) {
                requested = storeAuthHelper.parseHeaderStoreId(request);
            }
            Long currentStoreId = storeAuthHelper.resolveCurrentOwnedStoreId(userId, requested);
            if (currentStoreId == null) {
                throw new BusinessException("您不是商户类型的用户，暂时无法上架商品/商品分类");
            }
            product.setStoreId(String.valueOf(currentStoreId));
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
        if (product.getOrderNum() == null) {
            product.setOrderNum(0);
        }
        product.setDeleted(0);
        product.setCreatedTime(new Date());
        product.setCreatedBy(username);
        normalizeMemberPrice(product);
        validatePricingForPay(product, null);
        productMapper.addProduct(product);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findProduct(Product product, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        // 1. 解析JWT获取当前用户（游客无 token 时按普通用户 + 请求 storeId 查询）
        Long userId = jwtUtil.tryGetUserId(request);
        // 2. 查询用户身份：1普通用户 2商户用户 3管理员
        User user = userId == null ? null : userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();

        // 普通用户：按请求传入的 storeId 查询对应店铺商品（未传则空）
        if (identityType == null || identityType == 1) {
            if (product.getStoreId() == null || product.getStoreId().isEmpty()) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
        }
        // 商户用户：未传 storeId 或扫的是自己店，看本店；扫别人店则按请求 storeId 逛店
        if (identityType != null && identityType == 2) {
            String browseStoreId = storeAuthHelper.resolveMerchantBrowseStoreId(userId, product.getStoreId());
            if (browseStoreId == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            product.setStoreId(browseStoreId);
        }
        // 管理员(3)：不额外过滤，返回全部
        if (StoreExpireHelper.shouldHidePublicCatalog(identityType, storeMapper, product.getStoreId())) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }

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

    private void normalizeMemberPrice(Product product) {
        if (product == null) {
            return;
        }
        BigDecimal memberPrice = product.getMemberPrice();
        if (memberPrice == null) {
            return;
        }
        if (memberPrice.compareTo(BigDecimal.ZERO) <= 0) {
            product.setMemberPrice(null);
            product.setClearMemberPrice(true);
            return;
        }
        if (product.getPrice() != null && memberPrice.compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("会员价须低于常规价");
        }
        product.setClearMemberPrice(false);
    }

    /**
     * 价格、库存已改为选填：接入支付的店铺上架商品仍须有价格，否则用户下单会失败。
     * 更新时按「请求值优先，缺失取库中原值」判断，避免只改状态时误报。
     */
    private void validatePricingForPay(Product product, Product exist) {
        if (product == null) {
            return;
        }
        String storeIdText = product.getStoreId() != null
                ? product.getStoreId()
                : (exist == null ? null : exist.getStoreId());
        Long storeId = storeAuthHelper.parseStoreId(storeIdText);
        if (storeId == null) {
            return;
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null || !MiniappUiHelper.enablePay(store)) {
            return;
        }
        Integer status = product.getProductStatus() != null
                ? product.getProductStatus()
                : (exist == null ? 1 : exist.getProductStatus());
        boolean onShelf = status == null || status == 1;
        BigDecimal effectivePrice = product.getPrice() != null
                ? product.getPrice()
                : (exist == null ? null : exist.getPrice());
        if (onShelf && effectivePrice == null) {
            throw new BusinessException("该店铺已接入支付，商品价格不能为空");
        }
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
        normalizeMemberPrice(product);
        Product exist = product.getId() == null ? null : productMapper.selectById(product.getId());
        validatePricingForPay(product, exist);
        productMapper.updateProduct(product);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteProduct(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("product", idList);
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
