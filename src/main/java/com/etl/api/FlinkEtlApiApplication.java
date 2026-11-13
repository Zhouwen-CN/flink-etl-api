package com.etl.api;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
@MapperScan({"com.etl.api.sys.mapper", "com.etl.api.ai.mapper", "com.etl.api.etl.mapper"})
public class FlinkEtlApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlinkEtlApiApplication.class, args);
    }

}
