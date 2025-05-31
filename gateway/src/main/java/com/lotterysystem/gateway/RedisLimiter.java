package com.lotterysystem.gateway;

import cn.hutool.core.io.resource.ResourceUtil;
import com.lotterysystem.gateway.constant.LimiterType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RedisLimiter {

    private final RedisTemplate<String,String> redisTemplate;

    private final DefaultRedisScript<Boolean> LimiterScript = new DefaultRedisScript<>();

    @PostConstruct
    protected void init(){
        String limiterLua = ResourceUtil.readUtf8Str("Limiter.lua");
        LimiterScript.setScriptText(limiterLua);
        LimiterScript.setResultType(boolean.class);
    }

    public Boolean tryAccess(String resourceId,String userId,String QPS){

        return redisTemplate.execute(LimiterScript, Collections.emptyList(),resourceId + userId,QPS);

    }

    public Boolean tryAccess(LimiterType type, String Id){

        return redisTemplate.execute(LimiterScript, Collections.emptyList(),type.getType() + Id,type.getQps());

    }

}
