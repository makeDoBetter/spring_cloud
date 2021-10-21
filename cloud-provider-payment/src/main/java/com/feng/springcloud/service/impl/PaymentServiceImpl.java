package com.feng.springcloud.service.impl;

import com.feng.springcloud.dao.PaymentDao;
import com.feng.springcloud.entities.Payment;
import com.feng.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Description:
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 11:09
 * @since JDK 1.8
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment queryById(Long id) {
        return paymentDao.queryById(id);
    }
}
