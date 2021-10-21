package com.feng.springcloud.controller;

import com.feng.springcloud.service.FeignOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试Hystrix客户端通配服务降级
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/27 15:15
 * @since JDK 1.8
 */
@RestController
public class OrderHystrixController {
    @Resource
    FeignOrderService service;

    /**
     * 测试请求_正常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/hystrix/payment/getOk/{id}")
    public String getOk(@PathVariable("id") int id) {
        return service.getOk(id);
    }


    /**
     * 测试请求_超时
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/hystrix/payment/getTimeout/{id}")
    public String getTimeout(@PathVariable("id") int id) {
        return service.getTimeout(id);
    }
}
