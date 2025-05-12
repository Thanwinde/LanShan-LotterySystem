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

        //log.info("拦截请求：{}", request.getRequestURI());
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.error("拒绝访问:session为空");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无信息，未登录！");
            return false;
        }
        Object id = session.getAttribute("id");
        Object name = session.getAttribute("name");
        Object auth = session.getAttribute("auth");
        if (id == null) {
            log.error("拒绝访问:未找到登录用户信息");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无信息，未登录！");
            return false;
        }
        if(UserContext.getId() == null)
            UserContext.setId((Long) session.getAttribute("id"));
        if(UserContext.getName() == null)
            UserContext.setName((String) session.getAttribute("name"));
        if(UserContext.getAuth() == null)
            UserContext.setAuth((String) session.getAttribute("auth"));
        return true;
    }
}
