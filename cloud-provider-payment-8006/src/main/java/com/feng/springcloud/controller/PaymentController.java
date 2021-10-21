package com.feng.springcloud.controller;

import com.feng.springcloud.entities.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/12 17:24
 * @since JDK 1.8
 */
@Slf4j
@RestController
@RequestMapping(value = "/payment")
public class PaymentController {


    /**
     * 请求响应
     * @return String the String
     */
    @GetMapping
    public CommonResult findConsul(){
        return new CommonResult<>(200,"sever has reday" + UUID.randomUUID()) ;
    }
}
