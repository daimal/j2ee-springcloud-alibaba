package org.kehl.order.config;

import feign.Logger;
import org.kehl.order.interceptor.feign.CustomFeignInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * springcloud-alibaba
 * Feign的配置文件
 *
 * 全局配置： 当使用@Configuration的时候，会将配置作用于所有的服务提供方
 * 局部配置： 如果只想针对莫i一个服务进行配置，就不要加@Configuration
 * @author : kehl
 * @date : 2022-05-16 15:40
 **/
//@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

//    修改契约配置，支持Feign原生的注解
//    @Bean
//    public Contract feignContract(){
//        return new Contract.Default();
//    }
//

//超时时间配置
//    @Bean
//    public Request.Options options(){
//        return new Request.Options(5000,3000);
//    }


    /*
    自定义拦截器
     */
    @Bean
    public CustomFeignInterceptor feignAcceptGzipEncodingInterceptor(){
        return new CustomFeignInterceptor();
    }

}
