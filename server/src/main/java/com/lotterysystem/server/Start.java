package com.lotterysystem.server;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.registry.ConfigSupplierRegistry;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

/**
 * @author nsh
 * @data 2025/5/9 21:29
 * @description
 **/
@SpringBootApplication(scanBasePackages = {
        "com.lotterysystem.gateway",
        "com.lotterysystem.server"
})
@EnableTransactionManagement
@EnableAsync
@Slf4j
public class Start extends SpringBootServletInitializer {
    static {
        // 指定当前身份为 Token Client
        ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
        // 集群限流客户端配置，ClusterClientConfig 目前只支持配置请求超时
        ClusterClientConfig clientConfig = new ClusterClientConfig();
        clientConfig.setRequestTimeout(1000);
        ClusterClientConfigManager.applyNewConfig(clientConfig);
    }
    static {
        ClusterClientAssignConfig assignConfig = new ClusterClientAssignConfig();
        assignConfig.setServerHost("127.0.0.1");
        assignConfig.setServerPort(8002);
        // 先指定名称空间为 serviceA
        ConfigSupplierRegistry.setNamespaceSupplier(()->"serviceA");
        ClusterClientConfigManager.applyNewAssignConfig(assignConfig);
    }
    static {

    }


    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
        log.info("server started");

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Start.class);
    }
}
