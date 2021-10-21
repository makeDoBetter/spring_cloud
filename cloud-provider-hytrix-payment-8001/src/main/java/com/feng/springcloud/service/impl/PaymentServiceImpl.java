package com.feng.springcloud.service.impl;

import com.feng.springcloud.service.IPaymentService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * PaymentServiceImpl.class
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/25 15:38
 * @since JDK 1.8
 */
@Service
public class PaymentServiceImpl implements IPaymentService {

    @Value("${server.port}")
    int port;
    @Override
    public String getOk(int id) {
        return "调用getOk成功，id=" + id + "port=" + port;
    }

    @Override
    @HystrixCommand(fallbackMethod = "getTimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")})
    public String getTimeout(int id) {
        int timeout = 5;
        try {
            Thread.sleep(timeout*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "调用getTimeout成功，id=" + id + "port=" + port + "调用时长（秒）=" + timeout;
    }

    /**
     * hystrix服务降级处理器
     * @param id id
     * @return String String
     */
    public String getTimeoutHandler(int id){
        return Thread.currentThread().getName()+ "服务调用超时或系统内部异常，请稍后再试";
    }

    //------------------------服务熔断测试
    @Override
    @HystrixCommand(fallbackMethod = "getCircuitHandler", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),//开启断路器
            //设置使熔断判断逻辑开始工作的最小请求数，设置10个，只有9个请求，即使都是错误的也不会触发熔断机制
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),//窗口期，断路器底层采用滑动窗口的机制
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),//触发断路器的失败请求占的比例
    })
    public String getCircuit(int id) {
        if (id < 0){
            throw new RuntimeException("id不可为负数" + id);
        }
        return "调用getCircuit成功" + UUID.randomUUID().toString();
    }

    public String getCircuitHandler(int id){
        return "服务端异常，请稍后再试";
    }
}
