package com.example.springboottemplate.service.system;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.entity.system.OperLog;
import com.example.springboottemplate.mapper.system.OperLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 异步落库操作日志，供 AOP 调用（不走 Controller，避免循环）。
 */
@Service
public class OperLogRecordService {

    private static final Logger log = LoggerFactory.getLogger(OperLogRecordService.class);

    @Autowired
    private OperLogMapper operLogMapper;

    @Async
    public void recordAsync(OperLog operLog) {
        try {
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
}
