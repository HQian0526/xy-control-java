package com.example.springboottemplate.config;

import com.example.springboottemplate.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/api/auth/**",
                        // 仅放行微信登录；绑定手机号 /wx/bindPhone 需要 JWT
                        "/wx/login",
                        "/api/wx/login",
                        // 游客可浏览店铺分类/商品/其他业务/店铺信息
                        "/catagory/findCatagory",
                        "/api/catagory/findCatagory",
                        "/product/findProduct",
                        "/api/product/findProduct",
                        "/otherBusiness/findOtherBusiness",
                        "/api/otherBusiness/findOtherBusiness",
                        "/store/findStore",
                        "/api/store/findStore",
                        // 逛店可看满减/可领券（领取仍需登录）
                        "/mallPromo/active",
                        "/api/mallPromo/active",
                        "/mallCoupon/storeTemplates",
                        "/api/mallCoupon/storeTemplates",
                        // 微信支付异步通知（微信服务器直连，无 JWT）
                        "/mallOrder/wxPayNotify",
                        "/api/mallOrder/wxPayNotify",
                        "/mallOrder/wxRefundNotify",
                        "/api/mallOrder/wxRefundNotify",
                        "/upload-images/**",
                        "/api/upload-images/**",
                        "/error"
                );
    }
}
