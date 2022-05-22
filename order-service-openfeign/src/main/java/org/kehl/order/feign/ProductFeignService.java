package org.kehl.order.feign;

import org.kehl.order.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


/*
name--指定调用rest接口所对应的服务名
path--指定调用rest接口所在的StockController指定的RequestMapping
 */
@FeignClient(name = "product-service",path = "/product",configuration = FeignConfig.class)
public interface ProductFeignService {

//    声明需要调用的rest接口对应的方法
    @RequestMapping("/getProduct")
    public String getProduct();

}
