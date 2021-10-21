package com.feng.springcloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 支付模块远程调用
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/27 9:51
 * @since JDK 1.8
 */
@Component
@FeignClient(name = "cloud-provider-hytrix-payment", fallback = FeignHystrixFallBackService.class)
public interface FeignOrderService {

    /**
     * 测试请求_正常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/payment/getOk/{id}")
    String getOk(@PathVariable("id") int id);

    /**
     * 测试请求_超时
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/payment/getTimeout/{id}")
    String getTimeout(@PathVariable("id") int id);
}
