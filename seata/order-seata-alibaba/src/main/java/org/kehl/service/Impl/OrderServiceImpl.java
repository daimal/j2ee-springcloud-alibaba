package org.kehl.service.Impl;

import io.seata.spring.annotation.GlobalTransactional;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.kehl.api.StockService;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    StockService stockService;

    @GlobalTransactional
    @Override
    public Order createOrder(Order order) {
        System.out.println("orderService");
        orderMapper.insert(order);
        stockService.reduct(order.getProductId());
//        int a=1/0;
        return order;
    }

    @Override
    @Trace
    @Tag(key="all",value="returnedObj")
    public List<Order> all() throws InterruptedException {
//        测试skywalking的告警功能
        TimeUnit.SECONDS.sleep(2);
        return orderMapper.selectAll();
    }

    @Override
    @Trace
    @Tags({@Tag( key="Order",value="returnedObj"),
            @Tag(key="param",value="arg[0]"),
    })
    public Order getById(Integer id) {
        return orderMapper.selectByPrimaryKey(id);
    }

}
