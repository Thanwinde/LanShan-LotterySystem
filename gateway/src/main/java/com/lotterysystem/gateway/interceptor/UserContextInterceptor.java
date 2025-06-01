package com.lotterysystem.gateway.interceptor;

import com.lotterysystem.gateway.Limiter.RedisLimiter;
import com.lotterysystem.gateway.constant.AuthStatue;
import com.lotterysystem.gateway.constant.LimiterType;
import com.lotterysystem.gateway.util.MyJWTUtil;
import com.lotterysystem.gateway.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final RedisLimiter redisLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String token = request.getHeader("Authorization");

        if(token == null){
            log.error("无信息，未登录！");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);

        Map<String,Object> info = MyJWTUtil.parseToken(token);

        if(info == null){
            log.error("无信息，未登录！");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long userid = Long.valueOf(info.get("userId").toString()) ;
        String name = info.get("name").toString();
        Integer auth = Integer.valueOf(info.get("auth").toString()) ;

        boolean sign;
        String url = request.getRequestURI();
        if("/api/prize/grab".equals(url) || "/api/prize/passgrab".equals(url)) {
            //sign = GlobeLimiter.tryUserGrabAccess(userid) && GlobeLimiter.tryGlobalGrabAccess();
            sign = redisLimiter.tryAccess(LimiterType.GRABACTION, String.valueOf(userid));
        } else {
            if (auth == AuthStatue.USER.getCode()) {
                //sign = GlobeLimiter.tryUserAccess(userid);
                sign = redisLimiter.tryAccess(LimiterType.USERGLOBE, String.valueOf(userid));
            } else {
                if (auth == AuthStatue.ADMIN.getCode())
                    //sign = GlobeLimiter.tryAdminAccess(userid);
                    sign = redisLimiter.tryAccess(LimiterType.ADMINGLOBE, String.valueOf(userid));
                else
                    //sign = GlobeLimiter.tryBannedAccess(userid);
                    sign = redisLimiter.tryAccess(LimiterType.BANNEDGLOBE, String.valueOf(userid));
            }
        }

        if(!sign){
            log.info("触发限流！id:{}",userid);
            PrintWriter writer = response.getWriter();
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/text;charset=utf-8");
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
