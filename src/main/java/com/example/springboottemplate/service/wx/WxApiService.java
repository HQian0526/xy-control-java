package com.example.springboottemplate.service.wx;

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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WxApiService {

    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String GET_PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    @Autowired
    private WxTenantResolver wxTenantResolver;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public WxSessionResult code2Session(String code) {
        return code2Session(code, null);
    }

    public WxSessionResult code2Session(String code, String appId) {
        WxTenantContext tenant = wxTenantResolver.byAppId(appId);
        ensureMiniappReady(tenant);

        String url = UriComponentsBuilder.fromHttpUrl(JSCODE2SESSION_URL)
                .queryParam("appid", tenant.getAppId())
                .queryParam("secret", tenant.getAppSecret())
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

    public String getPhoneNumber(String phoneCode) {
        return getPhoneNumber(phoneCode, null);
    }

    public String getPhoneNumber(String phoneCode, String appId) {
        WxTenantContext tenant = wxTenantResolver.byAppId(appId);
        ensureMiniappReady(tenant);
        if (!StringUtils.hasText(phoneCode)) {
            throw new RuntimeException("手机号授权 code 不能为空");
        }

        try {
            return doGetPhoneNumber(phoneCode.trim(), tenant, getAccessToken(tenant, false));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 400) {
                clearToken(tenant.getAppId());
                return doGetPhoneNumber(phoneCode.trim(), tenant, getAccessToken(tenant, true));
            }
            throw new RuntimeException("调用微信手机号接口失败: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信手机号接口失败: " + e.getMessage(), e);
        }
    }

    private String doGetPhoneNumber(String phoneCode, WxTenantContext tenant, String accessToken) {
        try {
            String url = UriComponentsBuilder.fromUriString(GET_PHONE_NUMBER_URL)
                    .queryParam("access_token", accessToken)
                    .toUriString();

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
                    clearToken(tenant.getAppId());
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

    public JsonNode postWithAccessToken(String url, Object payload) {
        return postWithAccessToken(url, payload, null);
    }

    public JsonNode postWithAccessToken(String url, Object payload, String appId) {
        WxTenantContext tenant = wxTenantResolver.byAppId(appId);
        ensureMiniappReady(tenant);
        try {
            JsonNode node = doPostJson(url, payload, getAccessToken(tenant, false));
            int errcode = node.path("errcode").asInt(0);
            if (errcode == 40001 || errcode == 42001) {
                clearToken(tenant.getAppId());
                return doPostJson(url, payload, getAccessToken(tenant, true));
            }
            return node;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 400) {
                clearToken(tenant.getAppId());
                return doPostJson(url, payload, getAccessToken(tenant, true));
            }
            throw new RuntimeException("调用微信接口失败: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        }
    }

    private JsonNode doPostJson(String url, Object payload, String accessToken) {
        try {
            String requestUrl = UriComponentsBuilder.fromUriString(url)
                    .queryParam("access_token", accessToken)
                    .toUriString();
            String jsonBody = objectMapper.writeValueAsString(payload);
            byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentLength(bodyBytes.length);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String body = restTemplate.postForObject(requestUrl, entity, String.class);
            if (!StringUtils.hasText(body)) {
                throw new RuntimeException("微信接口无响应");
            }
            JsonNode node = objectMapper.readTree(body);
            int errcode = node.path("errcode").asInt(0);
            if (errcode == 40001 || errcode == 42001) {
                // caller will refresh
            }
            return node;
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    public String getAccessToken() {
        return getAccessToken(wxTenantResolver.platform(), false);
    }

    public synchronized String getAccessToken(boolean forceRefresh) {
        return getAccessToken(wxTenantResolver.platform(), forceRefresh);
    }

    private String getAccessToken(WxTenantContext tenant, boolean forceRefresh) {
        String cacheKey = tenant.getAppId();
        long now = System.currentTimeMillis();
        if (!forceRefresh) {
            CachedToken cached = tokenCache.get(cacheKey);
            if (cached != null && StringUtils.hasText(cached.token) && now < cached.expireAtMs) {
                return cached.token;
            }
        }

        ensureMiniappReady(tenant);
        String url = UriComponentsBuilder.fromUriString(ACCESS_TOKEN_URL)
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", tenant.getAppId())
                .queryParam("secret", tenant.getAppSecret())
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
            long expireAtMs = now + Math.max(60, expiresIn - 300) * 1000L;
            tokenCache.put(cacheKey, new CachedToken(token, expireAtMs));
            return token;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("获取微信 access_token 失败: " + e.getMessage(), e);
        }
    }

    private void clearToken(String appId) {
        if (StringUtils.hasText(appId)) {
            tokenCache.remove(appId);
        }
    }

    private void ensureMiniappReady(WxTenantContext tenant) {
        if (tenant == null || !tenant.miniappReady()) {
            throw new RuntimeException("未配置微信小程序 appId/appSecret");
        }
        if ("your-miniapp-appid".equals(tenant.getAppId())
                || "your-miniapp-appsecret".equals(tenant.getAppSecret())) {
            throw new RuntimeException("请先配置真实的微信小程序 appId/appSecret");
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static class CachedToken {
        private final String token;
        private final long expireAtMs;

        private CachedToken(String token, long expireAtMs) {
            this.token = token;
            this.expireAtMs = expireAtMs;
        }
    }
}
