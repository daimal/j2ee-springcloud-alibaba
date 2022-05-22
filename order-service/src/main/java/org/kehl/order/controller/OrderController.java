package org.kehl.order.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kehl.order.entity.Order;
import org.kehl.order.entity.Product;
import org.kehl.order.entity.User;
import org.kehl.order.service.ProductApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:27
 **/
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    private static final String PRODUCT_URL="https://192.168.137.1:8081/product";
    private static final String PRODUCT_SERVER="product-service";
    private static final String USER_URL="https://192.168.137.1:8071/user";
    private static final String USER_SERVER="user-service";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ProductApiService productApiService;

    @GetMapping("/product")
    public String getOrderInfo(){
        String result=restTemplate.getForObject("http://product-service/product/getProduct",String.class);

        System.out.println(result);
        return "success";
    }


    @GetMapping("/product/{pid}/userId/{uid}")
    public Order getOrderInfoByPidAndUid(@PathVariable("pid") String pid, @PathVariable("uid") String uid){
        Product product=restTemplate.getForObject("http://product-service/product/getProduct/"+pid,Product.class);
//        User user=restTemplate.getForObject(USER_URL+"/getUserInfo/"+uid,User.class);
        User user=restTemplate.getForObject("http://user-service/user/getUserInfo/"+uid,User.class);
        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
        log.info(">>用户信息,查询结果:" + JSON.toJSONString(user));
        Order order = new Order();
        order.setUid(user.getId());
        order.setUsername(user.getName());
        order.setPid(product.getId());
        order.setPname(product.getName());
        order.setPprice(product.getPrice());
        order.setNumber(1);
        return order;
    }


    /**
     * 使用nacos方式范问相关接口信息
     * @param pid,uid
     * @return Order
     */
    @GetMapping("/productNacos/{pid}/userId/{uid}")
    public Order productNacos(@PathVariable("pid") String pid,@PathVariable("uid") String uid){
        log.info(">>>客户下单，调用商品微服务查询商品信息<<<");
        // 采用restTemplate调用
        //从nacos中获取服务地址 获取的是个list集群信息

        ServiceInstance userInstance=discoveryClient.getInstances("user-service").get(0);
        String userUrl=userInstance.getHost()+":"+userInstance.getPort();
        log.info(">>从nacos中获取到的用户的微服务地址为:" + userUrl);
        User user=restTemplate.getForObject("http://"+userUrl+"/user/getUserInfo/"+uid,User.class);

        ServiceInstance productInstance = discoveryClient.getInstances("product-service").get(0);
        String productUrl = productInstance.getHost() + ":" +productInstance.getPort();
        log.info(">>从nacos中获取到的商品的微服务地址为:" + productUrl);
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);

        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
        log.info(">>用户信息,查询结果:" + JSON.toJSONString(user));
        Order order = new Order();
        order.setUid(user.getId());
        order.setUsername(user.getName());
        order.setPid(product.getId());
        order.setPname(product.getName());
        order.setPprice(product.getPrice());
        order.setNumber(1);
        return order;
    }

    /**
     * 使用ribbon方式范问相关接口信息
     * @param pid,uid
     * @return
     */
    @GetMapping("/productRibbon/{pid}/userId/{uid}")
    public Order productRibbon(@PathVariable("pid")String pid,@PathVariable("uid") String uid){
        log.info(">>>客户下单，调用商品微服务查询商品信息<<<");
        // 采用restTemplate调用
        //从nacos中获取服务地址 获取的是个list集群信息
        //此处我删除了用户相关，由于restTemplate 配置了ribbon注解，但是用户服务未配置ribbon，此处调用会报错
        /*ServiceInstance productInstance = discoveryClient.getInstances("product-service").get(0);
        String productUrl = productInstance.getHost() + ":" +productInstance.getPort();
        log.info(">>从nacos中获取到的商品的微服务地址为:" + productUrl);
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);
         */
        log.info(">>从Ribbon中范问商品的微服务地址为:");
        String productUrl = "product-service";
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);

        String userUrl = "user-service";
        User user = restTemplate.getForObject("http://"+userUrl+"/user/getUserInfo/"+uid, User.class);
        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
        log.info(">>用户信息,查询结果:" + JSON.toJSONString(user));
        Order order = new Order();
        order.setUid(user.getId());
        order.setUsername(user.getName());
        order.setPid(product.getId());
        order.setPname(product.getName());
        order.setPprice(product.getPrice());
        order.setNumber(1);
        return order;
    }
    /**
     * 基于fegin实现远程服务调用
     * @param pid,uid
     * @return
     */
    @GetMapping("/productFegin/{pid}")
    public Order order(@PathVariable("pid") String pid){
        log.info(">>>客户基于fegin调用商品微服务查询商品信息<<<");
        Product product = productApiService.getProductById(pid);
        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
        Order order = new Order();
        order.setUid("1");
        order.setUsername("测试用户1");
        order.setPid(product.getId());
        order.setPname(product.getName());
        order.setPprice(product.getPrice());
        order.setNumber(1);
        return order;
    }
}
