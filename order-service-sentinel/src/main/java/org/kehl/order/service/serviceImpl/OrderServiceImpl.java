package org.kehl.order.service.serviceImpl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.kehl.order.service.OrderService;
import org.springframework.stereotype.Service;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-17 17:30
 **/
@Service
public class OrderServiceImpl implements OrderService {
    @Override
    @SentinelResource(value = "getUser",blockHandler = "blockHandlerForGerUser")
    public String getUser() {
        return "查询用户";
    }

    //注意这里不要忘记加BlockException e !!!!
    public String blockHandlerForGerUser(BlockException e){
        e.printStackTrace();
        return "流控用户";
    }

    @Override
    public String adduser() {
        return "添加用户";
    }
}
