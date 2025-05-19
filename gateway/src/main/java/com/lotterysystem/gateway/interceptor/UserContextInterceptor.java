package com.lotterysystem.gateway.interceptor;

import com.lotterysystem.gateway.SentinelConfig.SentinelAdminLimiter;
import com.lotterysystem.gateway.SentinelConfig.SentinelUserLimiter;
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
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler){

        HttpSession session = request.getSession(false);

        if (session == null) {
            log.error("无信息，未登录！");
            return false;
        }

        Long userid = (Long) session.getAttribute("id");
        String name = (String) session.getAttribute("name");
        String auth = (String) session.getAttribute("auth");
        if (userid == null) {
            log.error("无信息，未登录！");
            return false;
        }
        boolean sign;
        if(auth.equals("user")){
            sign = SentinelUserLimiter.tryAccess(userid);
        } else{
            sign = SentinelAdminLimiter.tryAccess(userid);
        }
        if(!sign){
            log.info("限流！");
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
