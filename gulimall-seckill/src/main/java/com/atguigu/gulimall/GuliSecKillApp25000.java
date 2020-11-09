package com.atguigu.gulimall;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Justin
 */
@EnableFeignClients("com.atguigu.gulimall.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class,RabbitHealthIndicatorAutoConfiguration.class})
public class GuliSecKillApp25000 {

    public static void main(String[] args) {
        SpringApplication.run(GuliSecKillApp25000.class, args);
    }
}
