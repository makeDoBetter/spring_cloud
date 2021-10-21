package com.feng.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/9 15:01
 * @since JDK 1.8
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer01Main {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer01Main.class, args);
    }
}
