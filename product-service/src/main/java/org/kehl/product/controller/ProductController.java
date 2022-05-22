package org.kehl.product.controller;

import org.kehl.product.entity.Product;
import org.kehl.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:17
 **/
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductService productService;

    //引入配置文件中中服务的端口值
    @Value("${server.port}")
    String port;

    @GetMapping("/getProduct")
    public String getProduct(){
        //为了测试feign和sentinel集成的
        int a=1/0;
        return "success"+port;
    }

    @GetMapping("/getProduct/{pid}")
    public Product getProduct(@PathVariable("pid") String pid){
        System.out.println(port);
        return productService.getProductById(pid);
    }

}
