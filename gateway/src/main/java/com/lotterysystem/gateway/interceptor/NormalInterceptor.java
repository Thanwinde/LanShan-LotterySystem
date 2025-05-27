package com.lotterysystem.gateway.interceptor;



import com.lotterysystem.gateway.SentinelConfig.GlobeLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NormalInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        //String token = request.getHeader("authorization").substring(7);




        HttpSession session = request.getSession(true);

        String id = session.getId();

        if(!GlobeLimiter.tryNormalAccess(id)){
            log.info("触发登陆限流！");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }
        return true;
    }
}
