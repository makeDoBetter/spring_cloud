package com.feng.springcloud.controller;

import com.feng.springcloud.entities.CommonResult;
import com.feng.springcloud.entities.Payment;
import com.feng.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Description: 支付模块Controller
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 11:19
 * @since JDK 1.8
 */
@RestController
@Slf4j
public class PaymentController {
    @Value("${server.port}")
    private String port;
    @Resource
    private PaymentService service;

    /**
     * 新建支付单据
     * @param payment payment
     * @return CommonResult CommonResult
     */
    @PostMapping(value = "payment/create")
    public CommonResult create(@RequestBody Payment payment) {
        int count = service.create(payment);
        log.info("**********插入结果" + count);
        if (count > 0) {
            return new CommonResult(200, "插入数据成功, port  " + port, count);
        }else {
            return new CommonResult(500, "插入数据失败");
        }
    }

    /**
     * 根据主键查询支付单据
     * @param id id
     * @return CommonResult CommonResult
     */
    @GetMapping(value = "payment/queryById/{id}")
    public CommonResult queryById(@PathVariable("id") Long id) {
        Payment payment = service.queryById(id);
        log.info("**********查询结果" + payment);
        if (payment != null) {
            return new CommonResult(200, "查询数据成功,port  "+port, payment);
        }else {
            return new CommonResult(500, "查询数据失败");
        }
    }

    /**
     * 超时测试
     *
     * @return String String
     */
    @GetMapping(value = "/payment/getPort")
    public String getPort(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return port;
    }
}
