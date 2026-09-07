package com.example.springboottemplate.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 对象转JSON字符串
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON失败", e);
        }
    }

    // JSON字符串转对象
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转对象失败", e);
        }
    }

    /**
     * 兼容 PC 的 JSON 数组，以及微信小程序 DELETE 把数组转成的 { "0": id } 对象。
     */
    public static List<Long> parseIdList(JsonNode body) {
        List<Long> ids = new ArrayList<>();
        if (body == null || body.isNull() || body.isMissingNode()) {
            return ids;
        }
        if (body.isArray()) {
            body.forEach(node -> addId(ids, node));
            return ids;
        }
        if (body.isObject()) {
            if (body.has("idList")) {
                return parseIdList(body.get("idList"));
            }
            if (body.has("ids")) {
                return parseIdList(body.get("ids"));
            }
            if (body.has("id")) {
                addId(ids, body.get("id"));
                return ids;
            }
            int index = 0;
            while (body.has(String.valueOf(index))) {
                addId(ids, body.get(String.valueOf(index)));
                index++;
            }
            if (ids.isEmpty()) {
                Iterator<Map.Entry<String, JsonNode>> fields = body.fields();
                while (fields.hasNext()) {
                    addId(ids, fields.next().getValue());
                }
            }
            return ids;
        }
        addId(ids, body);
        return ids;
    }

    private static void addId(List<Long> ids, JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isNumber()) {
            ids.add(node.longValue());
            return;
        }
        String text = node.asText();
        if (!StringUtils.hasText(text)) {
            return;
        }
        ids.add(Long.parseLong(text.trim()));
    }
}
