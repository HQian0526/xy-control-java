package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.OtherBusiness;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.OtherBusinessMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.OtherBusinessService;
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
public class OtherBusinessServiceimpl implements OtherBusinessService {

    @Autowired
    private OtherBusinessMapper otherBusinessMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Response addOtherBusiness(OtherBusiness otherBusiness, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        Integer userId = jwtUtil.getUserId(claims);

        Store query = new Store();
        query.setUserId(userId == null ? null : Long.valueOf(userId));
        query.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(query);
        if (ValidateUtil.isEmpty(storeList)) {
            throw new BusinessException("您不是商户类型的用户，暂时无法添加其他业务");
        }

        Store store = storeList.get(0);
        if (store.getStoreId() != null) {
            otherBusiness.setStoreId(store.getStoreId());
        }

        otherBusiness.setId(IdWorker.getId());
        otherBusiness.setDeleted(0);
        otherBusiness.setCreatedTime(new Date());
        otherBusiness.setCreatedBy(username);
        otherBusinessMapper.addOtherBusiness(otherBusiness);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findOtherBusiness(OtherBusiness otherBusiness, Integer pageNum, Integer pageSize,
                                      HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtUtil.parseToken(token);
        Integer userId = jwtUtil.getUserId(claims);
        User user = userId == null ? null : userMapper.selectById(userId.longValue());
        Integer identityType = user == null ? null : user.getIdentityType();

        // 普通用户或身份未知：返回空数据
        if (identityType == null || identityType == 1) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        // 商户用户：仅返回本账号绑定商户下的业务
        if (identityType == 2) {
            Store storeQuery = new Store();
            storeQuery.setUserId(Long.valueOf(userId));
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
                return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
            }
            otherBusiness.setStoreId(storeList.get(0).getStoreId());
        }
        // 管理员(3)：不额外过滤，返回全部

        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<OtherBusiness> list = otherBusinessMapper.findOtherBusiness(otherBusiness);
        list.forEach(item -> {
            if (item.getStoreId() != null) {
                Store store = storeMapper.selectByStoreId(item.getStoreId());
                item.setStoreName(store != null ? store.getStoreName() : null);
            }
        });
        return buildPageResponse(list, pageNum, pageSize);
    }

    private Response buildPageResponse(List<OtherBusiness> list, Integer pageNum, Integer pageSize) {
        PageInfo<OtherBusiness> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateOtherBusiness(OtherBusiness otherBusiness, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        otherBusiness.setUpdateBy(username);
        otherBusiness.setUpdateTime(new Date());
        otherBusinessMapper.updateOtherBusiness(otherBusiness);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteOtherBusiness(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        Integer affectedRows = otherBusinessMapper.deleteBatchIds(idList);
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        }
        return new Response(400, null, "操作失败，未找到需要删除的记录");
    }
}
