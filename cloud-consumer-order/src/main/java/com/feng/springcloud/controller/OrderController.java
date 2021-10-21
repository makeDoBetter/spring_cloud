package com.feng.springcloud.controller;

import com.feng.springcloud.entities.CommonResult;
import com.feng.springcloud.entities.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 使用RestTemplate 的方式从订单客户端访问支付服务
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 15:15
 * @since JDK 1.8
 */
@RestController
public class OrderController {
    //private static final String PAYMENT_URL = "http://localhost:8001";
    //支付服务构建为多节点集群时，客户端调用不能在配置指定路径，而直接指定对应的微服务名称
    //使用微服务名称后，需要开启RestTemplate负载均衡功能，在注册bean时添加注解 @LoadBalanced
    private static final String PAYMENT_URL = "http://CLOUD-PROVIDER-PAYMENT/";
    @Resource
    private RestTemplate restTemplate;

    @GetMapping("order/payment/create")
    public CommonResult<Payment> create(Payment payment) {
        return restTemplate.postForObject(PAYMENT_URL + "payment/create", payment, CommonResult.class);
    }

    @GetMapping("order/payment/queryById/{id}")
    public CommonResult<Payment> queryById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "payment/queryById/" + id, CommonResult.class);
    }

    @GetMapping("order/payment/create2")
    public CommonResult<Payment> create2(Payment payment) {
        ResponseEntity<CommonResult> entity = restTemplate.postForEntity(PAYMENT_URL + "payment/create", payment, CommonResult.class);
        if (entity.getStatusCode().is2xxSuccessful()){
            return entity.getBody();
        }else {
            return new CommonResult<>(500, "服务调用失败");
        }
    }

    @GetMapping("order/payment/queryById2/{id}")
    public CommonResult<Payment> queryById2(@PathVariable("id") Long id) {
        ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_URL + "payment/queryById/" + id, CommonResult.class);
        if (entity.getStatusCode().is2xxSuccessful()){
            return entity.getBody();
        }else {
            return new CommonResult<>(500, "服务调用失败");
        }
    }
}
