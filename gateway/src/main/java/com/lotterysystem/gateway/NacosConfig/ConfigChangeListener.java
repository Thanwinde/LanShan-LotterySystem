package com.lotterysystem.gateway.NacosConfig;


import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.lotterysystem.gateway.Limiter.LimiterDetails;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author nsh
 * @data 2025/6/1 17:00
 * @description
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigChangeListener {

    private String dataId = "QPS.json";

    private String group = "LOTTERY";

    private final NacosConfigManager nacosConfigManager;

    private final LimiterDetails limiterDetails;

    @PostConstruct
    public void init() throws NacosException {

        //手动从nacos拉取路由配置
        //getConfigAndSignListener既可以拉取配置又可以设置监听器(响应配置更改)
        String config = nacosConfigManager.getConfigService().getConfigAndSignListener(
                dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor();
                    }

                    @Override
                    public void receiveConfigInfo(String s) {
                        limiterDetails.refurbishLimiterDetails(s);
                    }
                }
        );
        log.info("限流规则拉取到： {}", config);
        limiterDetails.refurbishLimiterDetails(config);
    }

}
