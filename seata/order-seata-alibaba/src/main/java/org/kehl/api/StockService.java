package org.kehl.api;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "stock-seata-alibaba-service",path = "/stock")
public interface StockService {
    @RequestMapping("/reduct")
    public String reduct(@RequestParam(value = "productId") String productId);
}
