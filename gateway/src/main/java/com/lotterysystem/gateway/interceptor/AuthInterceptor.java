package com.lotterysystem.gateway.interceptor;

import com.lotterysystem.gateway.util.UserContext;
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
public class AuthInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        log.info("拦截：{}", request.getRequestURI());
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.error("session为空");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无信息，未登录！");
            return false;
        }
        Object name = session.getAttribute("id");
        if (name == null) {
            log.error("未找到登录用户信息");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无信息，未登录！");
            return false;
        }
        UserContext.setId(session.getAttribute("id"));
        return true;
    }
}
