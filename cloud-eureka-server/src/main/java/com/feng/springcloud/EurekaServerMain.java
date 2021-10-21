package com.feng.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka 服务端主启动类
 *
 * 启动完成后访问 http://localhost:7001/ 可查看Eureka server端成功启动与否
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/9 11:12
 * @since JDK 1.8
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerMain.class, args);
    }
}
