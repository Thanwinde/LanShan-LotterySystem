package com.lotterysystem.sentinelservice;


import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class SentinelServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SentinelServiceApplication.class, args);
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();

        ClusterServerConfigManager.loadGlobalTransportConfig(new ServerTransportConfig()
                .setIdleSeconds(600)
                .setPort(8010));
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton("server"));

        // Start the server.
        tokenServer.start();
    }

}
