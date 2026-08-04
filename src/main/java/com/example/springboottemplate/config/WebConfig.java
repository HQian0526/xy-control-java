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
                        "/upload-images/**",
                        "/api/upload-images/**",
                        "/error"
                );
    }
}
