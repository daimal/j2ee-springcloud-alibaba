package org.kehl.controller;

import org.kehl.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.kehl.service.OrderService;

import java.util.List;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-19 14:51
 **/
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @RequestMapping("/add")
    public String add(){
        Order order =new Order();
        order.setProductId("9");
        order.setStatus(0);
        order.setTotalAmount(100);
        orderService.createOrder(order);
        return "下单成功";
    }

    @RequestMapping("all")
    public List<Order> all() throws InterruptedException {
        return orderService.all();
    }

    @RequestMapping("getById/{id}")
    public Order getById(@PathVariable("id") Integer id){
        return orderService.getById(id);
    }


    @RequestMapping("/header")
    public String header(@RequestHeader("X-Request-red") String color){
        return color;
    }

    @RequestMapping("/getParam")
    public String getParam(@RequestParam("red") String color){
        return color;
    }





}
