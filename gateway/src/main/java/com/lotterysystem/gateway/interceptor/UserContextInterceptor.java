package com.lotterysystem.gateway.interceptor;

import com.lotterysystem.gateway.SentinelConfig.GlobeLimiter;
import com.lotterysystem.gateway.constant.AuthStatue;
import com.lotterysystem.gateway.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            log.error("无信息，未登录！");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long userid = (Long) session.getAttribute("id");
        String name = (String) session.getAttribute("name");
        Integer auth = (Integer) session.getAttribute("auth");
        if (userid == null) {
            log.error("无信息，未登录！");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        boolean sign;
        String url = request.getRequestURI();
        if("/api/prize/grab".equals(url)){
            sign = GlobeLimiter.tryGrabAccess(userid);
        } else {
            if (auth == AuthStatue.USER.getCode()) {
                sign = GlobeLimiter.tryUserAccess(userid);
            } else {
                if (auth == AuthStatue.ADMIN.getCode())
                    sign = GlobeLimiter.tryAdminAccess(userid);
                else
                    sign = GlobeLimiter.tryBannedAccess(userid);
            }
        }
        if(!sign){
            log.info("触发全局限流！id:{}",userid);
            PrintWriter writer = response.getWriter();
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            writer.write("操作太快了，请稍等一会吧");
            writer.close();
            return false;
        }

        if(UserContext.getId() == null)
            UserContext.setId(userid);
        if(UserContext.getName() == null)
            UserContext.setName(name);
        if(UserContext.getAuth() == null)
            UserContext.setAuth(auth);
        return true;
    }
}
