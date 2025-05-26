package com.lotterysystem.gateway.config;



import com.lotterysystem.gateway.interceptor.NormalInterceptor;
import com.lotterysystem.gateway.interceptor.UserContextInterceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    final NormalInterceptor normalInterceptor;
    final UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
/*        SentinelWebMvcConfig config = new SentinelWebMvcConfig();
        config.setHttpMethodSpecify(true);

        registry.addInterceptor(new SentinelWebInterceptor(config))
                .addPathPatterns("/**");*/

        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/admin/login",
                        "/swagger-ui/**",
                        "/v3/**",
                        "/error"
                );

        registry.addInterceptor(normalInterceptor)
                .addPathPatterns(
                        "/api/admin/login",
                        "/swagger-ui/**",
                        "/v3/**");

    }


}
