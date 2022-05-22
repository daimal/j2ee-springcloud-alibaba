package org.kehl.service.Impl;

import org.kehl.entity.Stock;
import org.kehl.mapper.StockMapper;
import org.kehl.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-19 15:51
 **/
@Service
public class StockServiceImpl implements StockService {


    @Autowired
    StockMapper stockMapper;

    @Override
    public String insert(Stock stock) {
        int result= stockMapper.insert(stock);
        return "添加成功";
    }

    @Override
    public void reduct(String productId) {
        System.out.println("更新商品"+productId);
        System.out.println(stockMapper.reduct(productId));
    }
}
