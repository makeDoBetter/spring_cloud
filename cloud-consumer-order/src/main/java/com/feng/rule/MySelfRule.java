package com.feng.rule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ribbon替换负载均衡策略自动配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/13 15:12
 * @since JDK 1.8
 */
@Configuration
public class MySelfRule {

    /**
     * 自定义Ribbon负载均衡算法，此处使用随机算法
     * @return IRule the IRule
     */
    @Bean
    public IRule randomRule(){
        return new RandomRule();
    }
}
