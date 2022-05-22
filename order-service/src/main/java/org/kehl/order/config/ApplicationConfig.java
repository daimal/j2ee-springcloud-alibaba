package org.kehl.order.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:26
 **/
@Configuration
public class ApplicationConfig {
    @Bean
    //不同服务之间调用的时候要利用负载均衡器，因为rest本身是解析不了服务的名称和对应的地址的，需要依靠服务名称
//    负载均衡的策略默认是轮询
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }
}
