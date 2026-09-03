package com.example.springboottemplate.aspect;

import com.example.springboottemplate.annotation.OperLog;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.service.system.OperLogRecordService;
import com.example.springboottemplate.utils.IpUtils;
import com.example.springboottemplate.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Aspect
@Component
public class OperLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);

    @Autowired
    private OperLogRecordService operLogRecordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Around("@annotation(operLogAnno)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLogAnno) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            try {
                saveLog(joinPoint, operLogAnno, result, error, System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.warn("记录操作日志异常: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperLog anno, Object result, Throwable error, long costMs) {
        HttpServletRequest request = currentRequest();
        String ip = IpUtils.getClientIp(request);
        String operUser = resolveOperUser(request);
        if (!StringUtils.hasText(operUser)) {
            operUser = resolveOperUserFromArgs(joinPoint.getArgs());
        }
        if (!StringUtils.hasText(operUser)) {
            operUser = resolveOperUserFromResult(result);
        }

        int operResult = 1;
        String remark = anno.remark();
        if (error != null) {
            operResult = 2;
            String msg = error.getMessage();
            remark = StringUtils.hasText(remark) ? remark + "；" + msg : msg;
            // 登录失败时仍尽量记下尝试登录的用户名
            if (!StringUtils.hasText(operUser)) {
                operUser = resolveOperUserFromArgs(joinPoint.getArgs());
            }
        } else if (result instanceof Response) {
            Integer code = ((Response) result).getCode();
            if (code != null && code != 200) {
                operResult = 2;
                String msg = ((Response) result).getMsg();
                if (StringUtils.hasText(msg)) {
                    remark = StringUtils.hasText(remark) ? remark + "；" + msg : msg;
                }
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        if (!StringUtils.hasText(remark)) {
            remark = method + "，耗时" + costMs + "ms";
        } else {
            remark = remark + "，耗时" + costMs + "ms";
        }
        if (remark != null && remark.length() > 480) {
            remark = remark.substring(0, 480);
        }

        com.example.springboottemplate.entity.system.OperLog entity =
                com.example.springboottemplate.entity.system.OperLog.builder()
                        .operModule(anno.module())
                        .operType(anno.type())
                        .operUser(operUser)
                        .ipAddress(ip)
                        .ipLocation(IpUtils.resolveLocation(ip))
                        .operResult(operResult)
                        .operTime(new Date())
                        .remark(remark)
                        .createdTime(new Date())
                        .createdBy(operUser)
                        .deleted(0)
                        .build();
        operLogRecordService.recordAsync(entity);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String resolveOperUser(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        Object username = request.getAttribute("username");
        if (username instanceof String && StringUtils.hasText((String) username)) {
            return (String) username;
        }
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parseToken(auth.substring(7));
                if (claims != null && StringUtils.hasText(claims.getSubject())) {
                    return claims.getSubject();
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return "";
    }

    /** 账号登录入参 User.userName */
    private String resolveOperUserFromArgs(Object[] args) {
        if (args == null) {
            return "";
        }
        for (Object arg : args) {
            if (arg instanceof com.example.springboottemplate.entity.system.User) {
                String name = ((com.example.springboottemplate.entity.system.User) arg).getUserName();
                if (StringUtils.hasText(name)) {
                    return name.trim();
                }
            }
        }
        return "";
    }

    /** 登录成功响应里的 userInfo.userName */
    @SuppressWarnings("unchecked")
    private String resolveOperUserFromResult(Object result) {
        if (!(result instanceof Response)) {
            return "";
        }
        Object data = ((Response) result).getData();
        if (!(data instanceof java.util.Map)) {
            return "";
        }
        Object userInfo = ((java.util.Map<?, ?>) data).get("userInfo");
        if (userInfo instanceof java.util.Map) {
            Object userName = ((java.util.Map<?, ?>) userInfo).get("userName");
            if (userName != null && StringUtils.hasText(String.valueOf(userName))) {
                return String.valueOf(userName).trim();
            }
        }
        return "";
    }
}
