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
        log.info(">>????????????,????????????:" + JSON.toJSONString(product));
        log.info(">>????????????,????????????:" + JSON.toJSONString(user));
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
     * ??????nacos??????????????????????????????
     * @param pid,uid
     * @return Order
     */
    @GetMapping("/productNacos/{pid}/userId/{uid}")
    public Order productNacos(@PathVariable("pid") String pid,@PathVariable("uid") String uid){
        log.info(">>>??????????????????????????????????????????????????????<<<");
        // ??????restTemplate??????
        //???nacos????????????????????? ???????????????list????????????

        ServiceInstance userInstance=discoveryClient.getInstances("user-service").get(0);
        String userUrl=userInstance.getHost()+":"+userInstance.getPort();
        log.info(">>???nacos??????????????????????????????????????????:" + userUrl);
        User user=restTemplate.getForObject("http://"+userUrl+"/user/getUserInfo/"+uid,User.class);

        ServiceInstance productInstance = discoveryClient.getInstances("product-service").get(0);
        String productUrl = productInstance.getHost() + ":" +productInstance.getPort();
        log.info(">>???nacos??????????????????????????????????????????:" + productUrl);
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);

        log.info(">>????????????,????????????:" + JSON.toJSONString(product));
        log.info(">>????????????,????????????:" + JSON.toJSONString(user));
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
     * ??????ribbon??????????????????????????????
     * @param pid,uid
     * @return
     */
    @GetMapping("/productRibbon/{pid}/userId/{uid}")
    public Order productRibbon(@PathVariable("pid")String pid,@PathVariable("uid") String uid){
        log.info(">>>??????????????????????????????????????????????????????<<<");
        // ??????restTemplate??????
        //???nacos????????????????????? ???????????????list????????????
        //???????????????????????????????????????restTemplate ?????????ribbon????????????????????????????????????ribbon????????????????????????
        /*ServiceInstance productInstance = discoveryClient.getInstances("product-service").get(0);
        String productUrl = productInstance.getHost() + ":" +productInstance.getPort();
        log.info(">>???nacos??????????????????????????????????????????:" + productUrl);
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);
         */
        log.info(">>???Ribbon????????????????????????????????????:");
        String productUrl = "product-service";
        Product product = restTemplate.getForObject("http://"+productUrl+"/product/getProduct/"+pid, Product.class);

        String userUrl = "user-service";
        User user = restTemplate.getForObject("http://"+userUrl+"/user/getUserInfo/"+uid, User.class);
        log.info(">>????????????,????????????:" + JSON.toJSONString(product));
        log.info(">>????????????,????????????:" + JSON.toJSONString(user));
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
     * ??????fegin????????????????????????
     * @param pid,uid
     * @return
     */
    @GetMapping("/productFegin/{pid}")
    public Order order(@PathVariable("pid") String pid){
        log.info(">>>????????????fegin???????????????????????????????????????<<<");
        Product product = productApiService.getProductById(pid);
        log.info(">>????????????,????????????:" + JSON.toJSONString(product));
        Order order = new Order();
        order.setUid("1");
        order.setUsername("????????????1");
        order.setPid(product.getId());
        order.setPname(product.getName());
        order.setPprice(product.getPrice());
        order.setNumber(1);
        return order;
    }
}
