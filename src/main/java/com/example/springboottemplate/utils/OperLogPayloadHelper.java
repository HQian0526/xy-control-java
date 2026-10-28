package com.example.springboottemplate.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 采集操作日志的请求/响应，脱敏密码 token 等，并截断过长内容。
 */
@Component
public class OperLogPayloadHelper {

    private static final int MAX_CHARS = 16 * 1024;
    private static final String MASK = "******";
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

    private final ObjectMapper objectMapper;

    public OperLogPayloadHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String requestMethod(HttpServletRequest request) {
        return request == null ? null : request.getMethod();
    }

    public String requestUri(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String uri = request.getRequestURI();
        return StringUtils.hasText(uri) ? uri : null;
    }

    public String captureRequestParams(HttpServletRequest request, Object[] args) {
        Map<String, Object> query = queryMap(request);
        Object body = collectBody(args);
        if (query.isEmpty() && body == null) {
            return null;
        }
        if (query.isEmpty()) {
            return toMaskedJson(body);
        }
        if (body == null) {
            return toMaskedJson(query);
        }
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put("query", query);
        wrapped.put("body", body);
        return toMaskedJson(wrapped);
    }

    public String captureResponse(Object result, Throwable error) {
        if (error != null) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("error", true);
            payload.put("message", error.getMessage());
            return toMaskedJson(payload);
        }
        if (result == null) {
            return null;
        }
        return toMaskedJson(result);
    }

    private Map<String, Object> queryMap(HttpServletRequest request) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (request == null || request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
            return query;
        }
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            query.put(entry.getKey(), values.length == 1 ? values[0] : values);
        }
        return query;
    }

    private Object collectBody(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> bodies = new ArrayList<>();
        for (Object arg : args) {
            if (shouldSkipArg(arg)) {
                continue;
            }
            bodies.add(normalizeArg(arg));
        }
        if (bodies.isEmpty()) {
            return null;
        }
        return bodies.size() == 1 ? bodies.get(0) : bodies;
    }

    private boolean shouldSkipArg(Object arg) {
        if (arg == null) {
            return true;
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
            return true;
        }
        if (arg instanceof BindingResult || arg instanceof Errors) {
            return true;
        }
        if (arg instanceof InputStream || arg instanceof OutputStream) {
            return true;
        }
        if (arg instanceof byte[]) {
            return true;
        }
        return false;
    }

    private Object normalizeArg(Object arg) {
        if (arg instanceof MultipartFile) {
            return fileMeta((MultipartFile) arg);
        }
        if (arg instanceof MultipartFile[]) {
            List<Object> files = new ArrayList<>();
            for (MultipartFile file : (MultipartFile[]) arg) {
                if (file != null) {
                    files.add(fileMeta(file));
                }
            }
            return files;
        }
        return arg;
    }

    private Map<String, Object> fileMeta(MultipartFile file) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fileName", file.getOriginalFilename());
        meta.put("size", file.getSize());
        meta.put("contentType", file.getContentType());
        return meta;
    }

    private String toMaskedJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            mask(node);
            return truncate(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return truncate(String.valueOf(value));
        }
    }

    private void mask(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            List<String> keys = new ArrayList<>();
            Iterator<String> names = obj.fieldNames();
            while (names.hasNext()) {
                keys.add(names.next());
            }
            for (String key : keys) {
                if (isSensitive(key)) {
                    obj.put(key, MASK);
                } else {
                    mask(obj.get(key));
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                mask(child);
            }
        }
    }

    private boolean isSensitive(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String lower = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return lower.contains("password")
                || lower.contains("secret")
                || lower.contains("authorization")
                || lower.contains("privatekey")
                || lower.contains("apiv3")
                || "token".equals(lower)
                || lower.endsWith("token");
    }

    private String truncate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        if (text.length() <= MAX_CHARS) {
            return text;
        }
        return text.substring(0, MAX_CHARS) + TRUNCATED_SUFFIX;
    }
}
