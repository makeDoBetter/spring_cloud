package com.feng.springcloud.controller;

import com.feng.springcloud.entities.CommonResult;
import com.feng.springcloud.entities.Payment;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * 使用RestTemplate 的方式从订单客户端访问支付服务
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/13 10:33
 * @since JDK 1.8
 */
@RestController
@RequestMapping(value = "/order")
public class OrderController {
    //private static final String PAYMENT_URL = "http://localhost:8001";
    //支付服务构建为多节点集群时，客户端调用不能在配置指定路径，而直接指定对应的微服务名称
    //使用微服务名称后，需要开启RestTemplate负载均衡功能，在注册bean时添加注解 @LoadBalanced
    private static final String PAYMENT_URL = "http://cloud-provider-payment/";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping
    public CommonResult<Payment> query() {
        return restTemplate.getForObject(PAYMENT_URL + "payment", CommonResult.class);
    }
}
