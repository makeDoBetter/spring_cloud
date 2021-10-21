package com.feng.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/13 10:33
 * @since JDK 1.8
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderMain8007 {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain8007.class, args);
    }
}
