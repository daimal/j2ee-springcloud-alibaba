package org.kehl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-20 18:43
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class GatewaySentinelApplication {
    public static void main(String []args){
        SpringApplication.run(GatewaySentinelApplication.class,args);
    }
}
