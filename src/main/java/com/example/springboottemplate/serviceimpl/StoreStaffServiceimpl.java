package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StoreStaff;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StoreStaffMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.StoreStaffService;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreStaffServiceimpl implements StoreStaffService {

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Autowired
    private StoreStaffMapper storeStaffMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;

    @Override
    public Response addStoreStaff(StoreStaff record, HttpServletRequest request) {
        if (record == null) {
            throw new BusinessException("参数不能为空");
        }
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        Long storeId = storeAuthHelper.resolveWritableStoreId(operator, record.getStoreId());
        storeAuthHelper.assertCanWriteStore(operator, storeId);

        String phone = record.getPhone() == null ? "" : record.getPhone().trim();
        if (!phone.matches(PHONE_PATTERN)) {
            throw new BusinessException("请输入正确的手机号");
        }
        User staffUser = userMapper.selectByPhone(phone);
        if (staffUser == null || (staffUser.getDeleted() != null && staffUser.getDeleted() == 1)) {
            throw new BusinessException("未找到该手机号，请让员工先登录小程序并授权手机号");
        }
        Integer identityType = staffUser.getIdentityType();
        if (identityType == null || identityType != 1) {
            throw new BusinessException("只能绑定普通用户为员工");
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        if (store.getUserId() != null && store.getUserId().equals(staffUser.getId())) {
            throw new BusinessException("不能将本店商家账号添加为员工");
        }

        StoreStaff exist = storeStaffMapper.selectByUserId(staffUser.getId());
        if (exist != null) {
            if (storeId.equals(exist.getStoreId())) {
                throw new BusinessException("该用户已是本店员工");
            }
            throw new BusinessException("该用户已在其他店铺任职，请先解绑");
        }

        Date now = new Date();
        Integer status = record.getStatus() == null ? 1 : record.getStatus();
        if (status != 0 && status != 1) {
            throw new BusinessException("状态仅支持启用或停用");
        }
        StoreStaff insert = StoreStaff.builder()
                .id(IdWorker.getId())
                .storeId(storeId)
                .userId(staffUser.getId())
                .status(status)
                .remark(clipRemark(record.getRemark()))
                .createdBy(operator.getUsername())
                .createdTime(now)
                .deleted(0)
                .build();
        storeStaffMapper.addStoreStaff(insert);
        return Response.success();
    }

    @Override
    public Response findStoreStaff(StoreStaff record, Integer pageNum, Integer pageSize, HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        StoreStaff query = record == null ? new StoreStaff() : record;
        Long storeId = storeAuthHelper.applyListStoreScope(operator, query.getStoreId());
        if (operator.isMerchant() && storeId == null) {
            return buildPageResponse(Collections.emptyList(), pageNum, pageSize);
        }
        query.setStoreId(storeId);
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<StoreStaff> list = storeStaffMapper.findStoreStaff(query);
        if (list == null) {
            list = Collections.emptyList();
        }
        list.forEach(item -> storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName));
        return buildPageResponse(list, pageNum, pageSize);
    }

    @Override
    public Response updateStoreStaff(StoreStaff record, HttpServletRequest request) {
        if (record == null || record.getId() == null) {
            throw new BusinessException("请选择员工");
        }
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        StoreStaff exist = storeStaffMapper.selectById(record.getId());
        if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
            throw new BusinessException("员工不存在");
        }
        storeAuthHelper.assertCanWriteStore(operator, exist.getStoreId());
        if (record.getStatus() != null && record.getStatus() != 0 && record.getStatus() != 1) {
            throw new BusinessException("状态仅支持启用或停用");
        }
        StoreStaff patch = new StoreStaff();
        patch.setId(record.getId());
        patch.setStatus(record.getStatus());
        if (record.getRemark() != null) {
            patch.setRemark(clipRemark(record.getRemark()));
        }
        patch.setUpdateBy(operator.getUsername());
        patch.setUpdateTime(new Date());
        storeStaffMapper.updateStoreStaff(patch);
        return Response.success();
    }

    @Override
    public Response deleteStoreStaff(List<Long> idList, HttpServletRequest request) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        List<Long> allowed = new ArrayList<>();
        for (Long id : idList) {
            if (id == null) {
                continue;
            }
            StoreStaff exist = storeStaffMapper.selectById(id);
            if (exist == null || (exist.getDeleted() != null && exist.getDeleted() == 1)) {
                continue;
            }
            storeAuthHelper.assertCanWriteStore(operator, exist.getStoreId());
            allowed.add(id);
        }
        if (ValidateUtil.isEmpty(allowed)) {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("store_staff", allowed);
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        }
        return new Response(400, null, "操作失败，未找到需要删除的记录");
    }

    private String clipRemark(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String remark = raw.trim();
        return remark.length() > 200 ? remark.substring(0, 200) : remark;
    }

    private Response buildPageResponse(List<StoreStaff> list, Integer pageNum, Integer pageSize) {
        PageInfo<StoreStaff> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }
}
