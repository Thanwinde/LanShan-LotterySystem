package com.lotterysystem.gateway.config;

import com.lotterysystem.gateway.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    final AuthInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要检查的路径
                .excludePathPatterns(
                    "/api/admin/login",
                    "/swagger-ui/**"
                );
    }
}
