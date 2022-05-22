package org.kehl.service;

import org.kehl.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OrderService {

    Order createOrder(Order order);

    public List<Order> all() throws InterruptedException;

    public Order getById(Integer id);
}
