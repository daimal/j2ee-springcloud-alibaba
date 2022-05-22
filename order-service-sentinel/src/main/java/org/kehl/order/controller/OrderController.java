package org.kehl.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kehl.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

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
    OrderService orderService;

    @GetMapping("/add")
    public String addOrder(String id){
        System.out.println(1);
        return "添加订单"+id;
    }

    @GetMapping("/get")
    public String getOrder(String id){

        System.out.println(1);
        return "查询订单"+id;
    }


    //热点流控规则
    /*
    热点流控规则只能只用@sentinelResource注解
     */
    @GetMapping("/get/{id}")
    @SentinelResource(value = "getOrderById",blockHandler = "blockHandlerForGetOrderById")
    public String getOrderById(@PathVariable("id") String id){
        return "查询订单，id:"+id;
    }
    public String blockHandlerForGetOrderById(String id,BlockException e){
        e.printStackTrace();
        return "热点限流了";
    }

    @RequestMapping("test1")
    public String test1(){
        return orderService.getUser();
    }

    @RequestMapping("test2")
    public String test2(){
        return orderService.getUser();
    }

    @GetMapping("/flow")
//    @SentinelResource(value="flow",blockHandler = "blockHandlerFlow")
    public String flow(String id){
        System.out.println(2);
        return "流控测试"+id;
    }

//    当有一个线程正在被处理时，其他的线程访问是不会被接受的，会返回blockHandlerFlow的结果
    @GetMapping("/flowThread")
//    @SentinelResource(value="flowThread",blockHandler = "blockHandlerFlow")
    public String flowThread(String id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        System.out.println(2);
        return "线程数流控测试"+id;
    }

//    blockHandlerFlow还是要自己写的，规则可以用控制台进行修改，
//    这个String id不能少！
    public String blockHandlerFlow(String id,BlockException e){
        e.printStackTrace();
        return "流控";
    }




}
