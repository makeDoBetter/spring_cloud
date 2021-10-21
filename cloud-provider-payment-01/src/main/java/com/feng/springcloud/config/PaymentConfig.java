package com.feng.springcloud.config;

import com.feng.springcloud.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Description: 自动配置类
 *
 * token拦截器编码完成后，需要将token注册到spring容器中，需要实现 WebMvcConfigurer接口，
 * 配置拦截器拦截的请求及过滤请求
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 16:23
 * @since JDK 1.8
 */
@Configuration
public class PaymentConfig implements WebMvcConfigurer {

    /**
     * 自定义拦截器自动注册到容器中
     *
     * @return TokenInterceptor TokenInterceptor
     */
    @Bean
    public TokenInterceptor getTokenInterceptor(){
        return new TokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //关闭拦截器token鉴权
        //registry.addInterceptor(getTokenInterceptor()).addPathPatterns("/**");
    }
}
