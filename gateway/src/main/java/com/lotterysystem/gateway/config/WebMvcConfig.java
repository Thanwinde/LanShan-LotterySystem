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

                .addPathPatterns("/**")

                .excludePathPatterns(
                    "/api/admin/login",
                    "/swagger-ui/**",
                    "/v3/**",
                    "/error"
                );
    }
}
