package com.feng.springcloud.controller;

import com.feng.springcloud.service.FeignPaymentService;
import com.feng.springcloud.entities.CommonResult;
import com.feng.springcloud.entities.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * OrderController
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/17 15:53
 * @since JDK 1.8
 */
@RestController
@Slf4j
public class OrderController {
    @Resource
    FeignPaymentService paymentService;
    /**
     * 根据主键查询支付单据
     *
     * @param id id
     * @return CommonResult CommonResult
     */
    @GetMapping(value = "consumer/payment/queryById/{id}")
    public CommonResult<Payment> queryById(@PathVariable("id") Long id){
        return paymentService.queryById(id);
    }

    /**
     * 超时测试
     *
     * @return String String
     */
    @GetMapping(value = "consumer/payment/getPort")
    public String getPort(){
        return paymentService.getPort();
    }
}
