package com.feng.springcloud;

import com.feng.rule.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

/**
 * Description: 订单服务主启动类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 12:24
 * @since JDK 1.8
 */
@SpringBootApplication
@RibbonClient(value = "CLOUD-PROVIDER-PAYMENT", configuration = MySelfRule.class)
public class OrderMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain.class, args);
    }
}
