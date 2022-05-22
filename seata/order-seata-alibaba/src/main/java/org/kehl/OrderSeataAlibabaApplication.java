package org.kehl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-19 14:49
 **/
@SpringBootApplication
@EnableTransactionManagement //开启本地事务@Transactional
@EnableFeignClients
public class OrderSeataAlibabaApplication {
    public static void main(String []args){
        SpringApplication.run(OrderSeataAlibabaApplication.class,args);
    }


}
