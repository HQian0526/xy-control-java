package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.entity.Catagory;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.CatagoryMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.CatagoryService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.BrowseStoreHelper;
import com.example.springboottemplate.utils.LogicDeleteHelper;
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
public class CatagoryServiceimpl implements CatagoryService {

    @Autowired
    private CatagoryMapper catagoryMapper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Response addCatagory(Catagory catagory, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名和用户id
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Long userId = jwtUtil.getUserId(claims);
        // 3. 校验当前用户是否绑定商户
        Store query = new Store();
        query.setUserId(userId == null ? null : userId);
        query.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(query);
        if (ValidateUtil.isEmpty(storeList)) {
            throw new BusinessException("您不是商户类型的用户，暂时无法上架商品/商品分类");
        }
        // 4. 自动绑定商户storeId
        Store store = storeList.get(0);
        if (store.getStoreId() != null) {
            catagory.setStoreId(String.valueOf(store.getStoreId()));
        }
        // 5. 雪花算法自动生成分类id，排序号默认1
        catagory.setCatagoryId(IdWorker.getId());
        if (catagory.getOrderNum() == null) {
            catagory.setOrderNum(1);
        }
        catagory.setDeleted(0);
        catagory.setCreatedTime(new Date());
        catagory.setCreatedBy(username);
        catagoryMapper.addCatagory(catagory);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findCatagory(Catagory catagory, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        // 1. 解析JWT获取当前用户（游客无 token 时按普通用户 + 请求 storeId 查询）
        Long userId = jwtUtil.tryGetUserId(request);
        // 2. 查询用户身份：1普通用户 2商户用户 3管理员
        User user = userId == null ? null : userMapper.selectById(userId);
        Integer identityType = user == null ? null : user.getIdentityType();

        // 普通用户：按请求传入的 storeId 查询对应店铺分类（未传则空）
        if (identityType == null || identityType == 1) {
            if (catagory.getStoreId() == null || catagory.getStoreId().isEmpty()) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
        }
        // 商户用户：未传 storeId 或扫的是自己店，看本店；扫别人店则按请求 storeId 逛店
        if (identityType == 2) {
            String browseStoreId = BrowseStoreHelper
                    .resolveMerchantBrowseStoreId(userId, catagory.getStoreId(), storeMapper);
            if (browseStoreId == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            catagory.setStoreId(browseStoreId);
        }
        // 管理员(3)：不额外过滤，返回全部

        // 传了分页参数才开启分页，否则返回全部数据
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Catagory> list = catagoryMapper.findCatagory(catagory);
        // 添加自定义storeName字段
        list.forEach(item -> {
            if (item.getStoreId() != null && !item.getStoreId().isEmpty()) {
                try {
                    Store store = storeMapper.selectByStoreId(Long.parseLong(item.getStoreId()));
                    item.setStoreName(store != null ? store.getStoreName() : null);
                } catch (NumberFormatException e) {
                    item.setStoreName(null);
                }
            }
        });
        return buildPageResponse(list, pageNum, pageSize);
    }

    private Response buildPageResponse(List<Catagory> list, Integer pageNum, Integer pageSize) {
        PageInfo<Catagory> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateCatagory(Catagory catagory, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        catagory.setUpdateBy(username);
        catagory.setUpdateTime(new Date());
        catagoryMapper.updateCatagory(catagory);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteCatagory(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("product_catagory", idList);
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
