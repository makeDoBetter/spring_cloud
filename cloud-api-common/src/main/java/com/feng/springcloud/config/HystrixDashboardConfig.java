package com.feng.springcloud.config;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ServletRegistrationBean自动配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/29 11:29
 * @since JDK 1.8
 */
@Configuration
public class HystrixDashboardConfig {

    /**
     * 手动添加 ServletRegistrationBean，避免出现spring对hystrix dashboard的不兼容
     * 监控页面出现 Unable to connect to Command Metric Stream. 异常
     * @return ServletRegistrationBean bean
     */
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet servlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
