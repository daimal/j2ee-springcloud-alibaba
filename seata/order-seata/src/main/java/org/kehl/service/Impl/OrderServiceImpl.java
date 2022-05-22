package org.kehl.service.Impl;

import org.kehl.entity.Order;
import org.kehl.mapper.OrderMapper;
import org.kehl.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-19 14:56
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    RestTemplate restTemplate;

    @Transactional
    @Override
    public Order createOrder(Order order) {
        System.out.println("orderService");
        orderMapper.insert(order);
        MultiValueMap<String,Object> paramMap =new LinkedMultiValueMap<>();
        paramMap.add("productId",order.getProductId());
        String msg=restTemplate.postForObject("http://localhost:8066/stock/reduct",paramMap,String.class);
        int a=1/0;
        return order;

    }
}
