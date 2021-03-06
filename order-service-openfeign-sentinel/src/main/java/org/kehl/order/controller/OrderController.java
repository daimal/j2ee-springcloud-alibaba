package org.kehl.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.kehl.order.entity.Order;
import org.kehl.order.entity.Product;
import org.kehl.order.entity.User;
import org.kehl.order.feign.UserFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.kehl.order.feign.ProductFeignService;

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
public class  OrderController {
    private static final String PRODUCT_URL="https://192.168.137.1:8081/product";
    private static final String PRODUCT_SERVER="product-service";
    private static final String USER_URL="https://192.168.137.1:8071/user";
    private static final String USER_SERVER="user-service";


    @Autowired
    private DiscoveryClient discoveryClient;

//    @Autowired
//    private ProductApiService productApiService;

    @Qualifier("org.kehl.order.feign.ProductFeignService")
    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    UserFeignService userFeignService;

    @GetMapping("/getOrderInfo")
    public String getOrderInfo(){
        String ProductResult=productFeignService.getProduct();
        String UserResult=userFeignService.getUser("1");
        System.out.println("UserResult:"+UserResult);
        System.out.println("ProductResult:"+ProductResult);
        return "success";
    }

    @GetMapping("/add")
    public String addOrder(String id){
        System.out.println(1);
        return "????????????"+id;
    }

    @GetMapping("/get")
    public String getOrder(String id){

        System.out.println(1);
        return "????????????"+id;
    }


    //??????????????????
    /*
    ??????????????????????????????@sentinelResource??????
     */
    @GetMapping("/get/{id}")
    @SentinelResource(value = "getOrderById",blockHandler = "blockHandlerForGetOrderById")
    public String getOrderById(@PathVariable("id") String id){
        return "???????????????id:"+id;
    }
    public String blockHandlerForGetOrderById(String id, BlockException e){
        e.printStackTrace();
        return "???????????????";
    }

    @GetMapping("/flow")
//    @SentinelResource(value="flow",blockHandler = "blockHandlerFlow")
    public String flow(String id){
        System.out.println(2);
        return "????????????"+id;
    }

    //    ?????????????????????????????????????????????????????????????????????????????????????????????blockHandlerFlow?????????
    @GetMapping("/flowThread")
//    @SentinelResource(value="flowThread",blockHandler = "blockHandlerFlow")
    public String flowThread(String id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        System.out.println(2);
        return "?????????????????????"+id;
    }

    //    blockHandlerFlow???????????????????????????????????????????????????????????????
//    ??????String id????????????
    public String blockHandlerFlow(String id,BlockException e){
        e.printStackTrace();
        return "??????";
    }



}
