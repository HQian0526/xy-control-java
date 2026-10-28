package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.entity.system.OperLog;
import com.example.springboottemplate.mapper.system.OperLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步落库操作日志，供 AOP 调用（不走 Controller，避免循环）。
 */
@Service
public class OperLogRecordService {

    private static final Logger log = LoggerFactory.getLogger(OperLogRecordService.class);
    private static final long LOGIN_FAIL_DEBOUNCE_MS = 5 * 60 * 1000L;

    @Autowired
    private OperLogMapper operLogMapper;

    /** 同一 IP + 同类登录失败，5 分钟内只落 1 条，避免被刷库。 */
    private final ConcurrentHashMap<String, Long> loginFailSeen = new ConcurrentHashMap<>();

    @Async
    public void recordAsync(OperLog operLog) {
        try {
            if (shouldSkipLoginFail(operLog)) {
                return;
            }
            if (operLog.getId() == null) {
                operLog.setId(IdWorker.getId());
            }
            if (operLog.getOperTime() == null) {
                operLog.setOperTime(new Date());
            }
            if (operLog.getCreatedTime() == null) {
                operLog.setCreatedTime(new Date());
            }
            if (operLog.getDeleted() == null) {
                operLog.setDeleted(0);
            }
            if (operLog.getCreatedBy() == null) {
                operLog.setCreatedBy(operLog.getOperUser());
            }
            operLogMapper.addOperLog(operLog);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    private boolean shouldSkipLoginFail(OperLog operLog) {
        if (operLog == null || operLog.getOperResult() == null || operLog.getOperResult() != 2) {
            return false;
        }
        if (!"登录认证".equals(operLog.getOperModule())) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (loginFailSeen.size() > 20_000) {
            loginFailSeen.entrySet().removeIf(e -> now - e.getValue() > LOGIN_FAIL_DEBOUNCE_MS);
        }
        String key = String.valueOf(operLog.getIpAddress()) + "|" + normalizeLoginFailRemark(operLog.getRemark());
        Long prev = loginFailSeen.putIfAbsent(key, now);
        if (prev == null) {
            return false;
        }
        if (now - prev < LOGIN_FAIL_DEBOUNCE_MS) {
            return true;
        }
        return !loginFailSeen.replace(key, prev, now);
    }

    private String normalizeLoginFailRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return "";
        }
        String text = remark.replaceAll("，耗时\\d+ms", "");
        text = text.replaceAll("rid:\\s*[^,\\s)]+", "");
        text = text.replaceAll("\\(\\d+\\)", "");
        return text.replaceAll("\\s+", " ").trim();
    }
}
