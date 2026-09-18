package com.example.springboottemplate.service.feie;

import com.example.springboottemplate.config.FeieProperties;
import com.example.springboottemplate.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 飞鹅开放平台 HTTP 客户端。签名：sha1(user + ukey + stime)
 * 文档：http://help.feieyun.com/#/home/doc/zh
 */
@Slf4j
@Component
public class FeieClient {

    private final FeieProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public FeieClient(FeieProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean configured() {
        return properties.configured();
    }

    public void requireConfigured() {
        if (!configured()) {
            throw new BusinessException("平台飞鹅密钥未配置，请在 yml 填写 feie.ukey");
        }
    }

    public FeieResult addPrinter(String sn, String key, String remark) {
        MultiValueMap<String, String> form = baseForm("Open_printerAddlist");
        String name = StringUtils.hasText(remark) ? remark.trim().replace("#", "") : sn;
        form.add("printerContent", sn + "#" + key + "#" + name);
        return post(form);
    }

    public FeieResult deletePrinter(String sn) {
        MultiValueMap<String, String> form = baseForm("Open_printerDelList");
        form.add("snlist", sn);
        return post(form);
    }

    public FeieResult queryStatus(String sn) {
        MultiValueMap<String, String> form = baseForm("Open_queryPrinterStatus");
        form.add("sn", sn);
        return post(form);
    }

    public FeieResult printMsg(String sn, String content, int times) {
        MultiValueMap<String, String> form = baseForm("Open_printMsg");
        form.add("sn", sn);
        form.add("content", content);
        form.add("times", String.valueOf(Math.max(1, Math.min(times, 5))));
        return post(form);
    }

    private MultiValueMap<String, String> baseForm(String apiName) {
        requireConfigured();
        long stime = System.currentTimeMillis() / 1000;
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("user", properties.getUser().trim());
        form.add("stime", String.valueOf(stime));
        form.add("sig", sign(stime));
        form.add("apiname", apiName);
        return form;
    }

    private String sign(long stime) {
        try {
            String raw = properties.getUser().trim() + properties.getUkey().trim() + stime;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new BusinessException("飞鹅签名失败");
        }
    }

    private FeieResult post(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        String apiName = form.getFirst("apiname");
        try {
            String body = restTemplate.postForObject(properties.getApiUrl(), entity, String.class);
            FeieResult result = parse(body);
            if (!result.ok()) {
                log.warn("飞鹅接口失败 api={} ret={} msg={} data={}",
                        apiName, result.ret(), result.msg(), result.data());
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("飞鹅接口异常 api={}", apiName, e);
            throw new BusinessException("飞鹅接口调用失败: " + e.getMessage());
        }
    }

    private FeieResult parse(String body) {
        if (!StringUtils.hasText(body)) {
            throw new BusinessException("飞鹅接口无返回");
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            int ret = node.path("ret").asInt(-1);
            String msg = node.path("msg").asText("");
            JsonNode dataNode = node.get("data");
            String data = dataNode == null || dataNode.isNull()
                    ? ""
                    : (dataNode.isTextual() ? dataNode.asText() : dataNode.toString());
            return new FeieResult(ret, msg, data);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("飞鹅返回无法解析: " + body);
        }
    }

    public record FeieResult(int ret, String msg, String data) {
        public boolean ok() {
            return ret == 0;
        }

        public String display() {
            if (StringUtils.hasText(data) && !"null".equals(data)) {
                return data;
            }
            return StringUtils.hasText(msg) ? msg : ("ret=" + ret);
        }
    }
}
