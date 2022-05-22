package org.kehl.service;

import org.kehl.entity.Order;
import org.springframework.stereotype.Service;

public interface OrderService {

    Order createOrder(Order order);

}
