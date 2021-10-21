package com.feng.springcloud.service;

import com.feng.springcloud.entities.CommonResult;
import com.feng.springcloud.entities.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 创建调用接口方法声明，使用OpenFeign动态代理实现其他服务接口调用
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/17 15:50
 * @since JDK 1.8
 */
@Component
@FeignClient(value = "CLOUD-PROVIDER-PAYMENT")
public interface FeignPaymentService {
    /**
     * 根据主键查询支付单据
     *
     * @param id id
     * @return CommonResult CommonResult
     */
    @GetMapping(value = "payment/queryById/{id}")
    CommonResult<Payment> queryById(@PathVariable("id") Long id);

    /**
     * 超时测试
     *
     * @return String String
     */
    @GetMapping(value = "/payment/getPort")
    String getPort();
}
