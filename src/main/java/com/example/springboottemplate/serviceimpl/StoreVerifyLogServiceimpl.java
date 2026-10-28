package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.StoreVerifyLog;
import com.example.springboottemplate.mapper.StoreVerifyLogMapper;
import com.example.springboottemplate.service.StoreVerifyLogService;
import com.example.springboottemplate.utils.StoreAuthHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StoreVerifyLogServiceimpl implements StoreVerifyLogService {

    @Autowired
    private StoreVerifyLogMapper storeVerifyLogMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;

    @Override
    public Response findStoreVerifyLog(StoreVerifyLog record, Integer pageNum, Integer pageSize,
                                       HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        StoreVerifyLog query = record == null ? new StoreVerifyLog() : record;
        Long storeId = storeAuthHelper.applyListStoreScope(operator, query.getStoreId());
        if (operator.isMerchant() && storeId == null) {
            PageInfo<StoreVerifyLog> empty = new PageInfo<>(Collections.emptyList());
            Map<String, Object> data = new HashMap<>();
            data.put("list", empty.getList());
            data.put("total", 0);
            data.put("pages", 0);
            data.put("pageNum", pageNum != null ? pageNum : 1);
            data.put("pageSize", pageSize != null ? pageSize : 10);
            return new Response(200, data, "操作成功");
        }
        query.setStoreId(storeId);
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<StoreVerifyLog> list = storeVerifyLogMapper.findStoreVerifyLog(query);
        if (list == null) {
            list = Collections.emptyList();
        }
        list.forEach(item -> {
            storeAuthHelper.fillStoreName(item.getStoreId(), item::setStoreName);
            item.setOperatorTypeName(operatorTypeName(item.getOperatorType()));
        });
        PageInfo<StoreVerifyLog> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return new Response(200, data, "操作成功");
    }

    private String operatorTypeName(Integer type) {
        if (type == null) {
            return "";
        }
        if (type == 2) {
            return "员工";
        }
        if (type == 3) {
            return "管理员";
        }
        return "商家";
    }
}
