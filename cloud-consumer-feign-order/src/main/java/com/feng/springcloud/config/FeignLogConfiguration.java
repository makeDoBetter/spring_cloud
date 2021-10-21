package com.feng.springcloud.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign日志级别配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/17 17:07
 * @since JDK 1.8
 */
@Configuration
public class FeignLogConfiguration {
    @Bean
    public Logger.Level level(){
        return Logger.Level.FULL;
    }
}
