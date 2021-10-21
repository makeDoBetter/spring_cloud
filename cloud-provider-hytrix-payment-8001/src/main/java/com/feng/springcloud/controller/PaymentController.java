package com.feng.springcloud.controller;

import com.feng.springcloud.service.IPaymentService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/25 15:45
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {
    @Autowired
    private IPaymentService service;

    @GetMapping("/getOk/{id}")
    public String getOk(@PathVariable("id") int id) {
        return service.getOk(id);
    }

    @GetMapping("/getTimeout/{id}")
    public String getTimeout(@PathVariable("id") int id) {
        String timeout = service.getTimeout(id);
        log.info(timeout);
        return timeout;
    }

    /**
     * 服务熔断测试handler
     * @param id id
     * @return String String
     */
    @GetMapping("/getCircuit/{id}")
    public String getCircuit(@PathVariable("id") int id) {
        return service.getCircuit(id);
    }
}
