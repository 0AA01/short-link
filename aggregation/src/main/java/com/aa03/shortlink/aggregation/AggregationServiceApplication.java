package com.aa03.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接聚合应用
 */
@SpringBootApplication(scanBasePackages = {
        "com.aa03.shortlink.admin",
        "com.aa03.shortlink.project",
        "com.aa03.shortlink.aggregation"
})
@EnableDiscoveryClient
@MapperScan(value = {
        "com.aa03.shortlink.project.dao.mapper",
        "com.aa03.shortlink.admin.dao.mapper"
})
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}