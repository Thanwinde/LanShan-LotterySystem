package com.lotterysystem.gateway.Limiter;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nsh
 * @data 2025/6/1 13:37
 * @description
 **/
@Slf4j
@Configuration
public class LimiterDetails {

    static public Map<String,String> QPSMap = new HashMap<String,String>();

    public void refurbishLimiterDetails(String json) {
        log.info("Nacos更新限流配置配置: {}",json);
        QPSMap = JSONUtil.toBean(json, HashMap.class);
    }
}
