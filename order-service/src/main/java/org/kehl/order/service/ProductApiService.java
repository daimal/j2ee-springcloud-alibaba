package org.kehl.order.service;


import org.kehl.order.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("product-service")//声明调用的提供者的名称
public interface ProductApiService {
    /**
     * 指定调用提供者的哪个方法
     * @FeignClient+@GetMapping 就是一个完整的请求路径 http://product-service/product/getProduct/{pid}
     * 会把提供者的名称自动转化为对应的ip地址
     * @param pid
     * @return
     */
    @GetMapping("/product/getProduct/{pid}")
    Product getProductById(@PathVariable("pid") String pid);
}
