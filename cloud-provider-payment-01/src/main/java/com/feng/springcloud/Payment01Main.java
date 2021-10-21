package com.feng.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/7 17:51
 * @since JDK 1.8
 */
@SpringBootApplication
@EnableEurekaClient
public class Payment01Main {
    public static void main(String[] args) {
        SpringApplication.run(Payment01Main.class, args);
    }
}
