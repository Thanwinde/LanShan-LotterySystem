package com.lotterysystem.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
        log.info("server started");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Start.class);
    }
}
