package com.example.springboottemplate.serviceimpl.system;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.entity.system.OperLog;
import com.example.springboottemplate.mapper.system.OperLogMapper;
import com.example.springboottemplate.service.system.OperLogService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.LogicDeleteHelper;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OperLogServiceimpl implements OperLogService {

    @Autowired
    private OperLogMapper operLogMapper;
    @Autowired
    private LogicDeleteHelper logicDeleteHelper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Response addOperLog(OperLog operLog, HttpServletRequest request) {
        String username = resolveUsername(request);
        operLog.setId(IdWorker.getId());
        if (operLog.getOperTime() == null) {
            operLog.setOperTime(new Date());
        }
        if (!StringUtils.hasText(operLog.getOperUser())) {
            operLog.setOperUser(username);
        }
        if (operLog.getOperResult() == null) {
            operLog.setOperResult(1);
        }
        operLog.setCreatedTime(new Date());
        operLog.setCreatedBy(username);
        if (operLog.getDeleted() == null) {
            operLog.setDeleted(0);
        }
        operLogMapper.addOperLog(operLog);
        return Response.success();
    }

    @Override
    public Response findOperLog(OperLog operLog, Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<OperLog> list = operLogMapper.findOperLog(operLog == null ? new OperLog() : operLog);
        PageInfo<OperLog> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateOperLog(OperLog operLog, HttpServletRequest request) {
        if (operLog == null || operLog.getId() == null) {
            return Response.fail(400, "日志ID不能为空");
        }
        operLog.setUpdateBy(resolveUsername(request));
        operLogMapper.updateOperLog(operLog);
        return Response.success();
    }

    @Override
    public Response deleteOperLog(List<Long> idList) {
        if (ValidateUtil.isEmpty(idList)) {
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = logicDeleteHelper.deleteByIds("oper_log", idList);
        if (affectedRows > 0) {
            return Response.success();
        }
        return new Response(400, null, "操作失败，未找到需要删除的记录");
    }

    private String resolveUsername(HttpServletRequest request) {
        Object attr = request.getAttribute("username");
        if (attr instanceof String && StringUtils.hasText((String) attr)) {
            return (String) attr;
        }
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parseToken(auth.substring(7));
            return claims.getSubject();
        }
        return null;
    }
}
