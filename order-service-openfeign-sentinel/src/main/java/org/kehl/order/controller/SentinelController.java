package org.kehl.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.kehl.order.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:53
 **/
@Slf4j
@RestController
@RequestMapping("/sentinel")
public class SentinelController {


    @Qualifier("org.kehl.order.feign.ProductFeignService")
    @Autowired
    ProductFeignService productFeignService;

    @RequestMapping("/getProduct")
    public String getProdcut(){
        String result= productFeignService.getProduct();
        return "Feign+Sentinel"+result;
    }

}
