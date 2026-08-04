package com.example.springboottemplate.service.wx;

import com.example.springboottemplate.config.WxProperties;
import com.example.springboottemplate.dto.WxSessionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WxApiService {

    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String GET_PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    @Autowired
    private WxProperties wxProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile String cachedAccessToken;
    private volatile long accessTokenExpireAtMs;

    public WxSessionResult code2Session(String code) {
        ensureWxConfigured();

        String url = UriComponentsBuilder.fromHttpUrl(JSCODE2SESSION_URL)
                .queryParam("appid", wxProperties.getAppId())
                .queryParam("secret", wxProperties.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class);
            if (!StringUtils.hasText(body)) {
                throw new RuntimeException("微信登录接口无响应");
            }
            JsonNode node = objectMapper.readTree(body);
            WxSessionResult result = new WxSessionResult();
            result.setOpenid(text(node, "openid"));
            result.setSessionKey(text(node, "session_key"));
            result.setUnionid(text(node, "unionid"));
            if (node.has("errcode") && !node.get("errcode").isNull()) {
                result.setErrcode(node.get("errcode").asInt());
            }
            result.setErrmsg(text(node, "errmsg"));
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信登录接口失败: " + e.getMessage(), e);
        }
    }

    /**
     * 用 getPhoneNumber 返回的 code 换取用户手机号（新版接口）
     */
    public String getPhoneNumber(String phoneCode) {
        ensureWxConfigured();
        if (!StringUtils.hasText(phoneCode)) {
            throw new RuntimeException("手机号授权 code 不能为空");
        }

        try {
            return doGetPhoneNumber(phoneCode.trim(), getAccessToken(false));
        } catch (HttpClientErrorException e) {
            // access_token 偶发失效时清缓存重试一次
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 400) {
                cachedAccessToken = null;
                accessTokenExpireAtMs = 0;
                return doGetPhoneNumber(phoneCode.trim(), getAccessToken(true));
            }
            throw new RuntimeException("调用微信手机号接口失败: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信手机号接口失败: " + e.getMessage(), e);
        }
    }

    private String doGetPhoneNumber(String phoneCode, String accessToken) {
        try {
            String url = UriComponentsBuilder.fromUriString(GET_PHONE_NUMBER_URL)
                    .queryParam("access_token", accessToken)
                    .toUriString();

            // Spring 6.1+ 默认不再缓冲请求体、不带 Content-Length；微信会因此返回 412
            String jsonBody = objectMapper.writeValueAsString(Map.of("code", phoneCode));
            byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentLength(bodyBytes.length);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String body = restTemplate.postForObject(url, entity, String.class);
            if (!StringUtils.hasText(body)) {
                throw new RuntimeException("微信手机号接口无响应");
            }

            JsonNode node = objectMapper.readTree(body);
            int errcode = node.path("errcode").asInt(0);
            if (errcode != 0) {
                if (errcode == 40001 || errcode == 42001) {
                    cachedAccessToken = null;
                    accessTokenExpireAtMs = 0;
                }
                throw new RuntimeException("获取微信手机号失败: " + text(node, "errmsg") + "(" + errcode + ")");
            }

            JsonNode phoneInfo = node.get("phone_info");
            if (phoneInfo == null || phoneInfo.isNull()) {
                throw new RuntimeException("获取微信手机号失败: 未返回 phone_info");
            }

            String purePhone = text(phoneInfo, "purePhoneNumber");
            if (StringUtils.hasText(purePhone)) {
                return purePhone;
            }
            String phoneNumber = text(phoneInfo, "phoneNumber");
            if (!StringUtils.hasText(phoneNumber)) {
                throw new RuntimeException("获取微信手机号失败: 手机号为空");
            }
            return phoneNumber;
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信手机号接口失败: " + e.getMessage(), e);
        }
    }

    public String getAccessToken() {
        return getAccessToken(false);
    }

    public synchronized String getAccessToken(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && StringUtils.hasText(cachedAccessToken) && now < accessTokenExpireAtMs) {
            return cachedAccessToken;
        }

        ensureWxConfigured();
        String url = UriComponentsBuilder.fromUriString(ACCESS_TOKEN_URL)
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", wxProperties.getAppId())
                .queryParam("secret", wxProperties.getAppSecret())
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class);
            if (!StringUtils.hasText(body)) {
                throw new RuntimeException("获取微信 access_token 无响应");
            }
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new RuntimeException("获取微信 access_token 失败: "
                        + text(node, "errmsg") + "(" + node.get("errcode").asInt() + ")");
            }
            String token = text(node, "access_token");
            int expiresIn = node.path("expires_in").asInt(7200);
            if (!StringUtils.hasText(token)) {
                throw new RuntimeException("获取微信 access_token 失败: token 为空");
            }
            cachedAccessToken = token;
            // 提前 5 分钟过期，避免临界失效
            accessTokenExpireAtMs = now + Math.max(60, expiresIn - 300) * 1000L;
            return cachedAccessToken;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("获取微信 access_token 失败: " + e.getMessage(), e);
        }
    }

    private void ensureWxConfigured() {
        if (!StringUtils.hasText(wxProperties.getAppId()) || !StringUtils.hasText(wxProperties.getAppSecret())) {
            throw new RuntimeException("未配置微信小程序 appId/appSecret");
        }
        if ("your-miniapp-appid".equals(wxProperties.getAppId())
                || "your-miniapp-appsecret".equals(wxProperties.getAppSecret())) {
            throw new RuntimeException("请先在 application.yml 中配置真实的微信小程序 appId/appSecret");
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
