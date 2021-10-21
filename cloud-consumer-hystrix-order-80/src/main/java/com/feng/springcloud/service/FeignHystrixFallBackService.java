package com.feng.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * FeignOrderService api中单个方法服务降级实现
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/28 15:24
 * @since JDK 1.8
 */
@Component
public class FeignHystrixFallBackService implements FeignOrderService{
    @Override
    public String getOk(int id) {
        return "服务端出现故障，请稍后再试";
    }

    @Override
    public String getTimeout(int id) {
        return "服务端出现故障，请稍后再试";
    }
}
