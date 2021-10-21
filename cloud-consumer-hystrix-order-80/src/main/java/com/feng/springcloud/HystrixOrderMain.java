package com.feng.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description: 订单服务主启动类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 12:24
 * @since JDK 1.8
 */
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class HystrixOrderMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixOrderMain.class, args);
    }
}
