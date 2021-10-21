package com.feng.springcloud.dao;

import com.feng.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 10:55
 * @since JDK 1.8
 */
@Mapper
public interface PaymentDao {

    /**
     * 新建支付单据
     * @param payment payment实体
     * @return int 成功值
     */
    int create(Payment payment);

    /**
     * 根据主键查询支付信息
     * @param id 主键
     * @return Payment 支付信息
     */
    Payment queryById(@Param("id") Long id);
}
