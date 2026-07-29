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
        // 1. 解析JWT获取当前用户
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtUtil.parseToken(token);
        Integer userId = jwtUtil.getUserId(claims);
        // 2. 查询用户身份：1普通用户 2商户用户 3管理员
        User user = userId == null ? null : userMapper.selectById(userId.longValue());
        Integer identityType = user == null ? null : user.getIdentityType();

        // 普通用户或身份未知：返回空数据
        if (identityType == null || identityType == 1) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        // 商户用户：仅返回本账号绑定商户下的分类
        if (identityType == 2) {
            Store storeQuery = new Store();
            storeQuery.setUserId(Long.valueOf(userId));
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            catagory.setStoreId(String.valueOf(storeList.get(0).getStoreId()));
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
        Integer affectedRows = catagoryMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
