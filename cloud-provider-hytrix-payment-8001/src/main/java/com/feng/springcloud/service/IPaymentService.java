package com.feng.springcloud.service;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/25 15:37
 * @since JDK 1.8
 */
public interface IPaymentService {

    /**
     * 测试请求_正常
     * @param id id
     * @return String String
     */
    String getOk(int id);

    /**
     * 测试请求_超时
     * @param id id
     * @return String String
     */
    String getTimeout(int id);

    /**
     * 测试服务熔断
     * @param id id
     * @return String String
     */
    String getCircuit(int id);
}
