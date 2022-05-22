package org.kehl.order;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.kehl.ribbon.ribbonRandomRuleConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients    //开启fegin
//@RibbonClients(value = {
//        @RibbonClient(name = "product-service",configuration = ribbonRandomRuleConfig.class)
//})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }
}
