package com.lotterysystem.gateway.interceptor;


import com.lotterysystem.gateway.SentinelConfig.SentinelNormalLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NormalInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)  {

        HttpSession session = request.getSession(true);

        String id = session.getId();

        if(!SentinelNormalLimiter.tryAccess(id)){
            log.info("登陆限流！");
            return false;
        }
        return true;
    }
}
