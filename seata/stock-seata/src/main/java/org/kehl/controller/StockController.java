package org.kehl.controller;

import org.kehl.entity.Stock;
import org.kehl.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-19 15:47
 **/
@RestController
@RequestMapping("/stock")
public class StockController {


    @Autowired
    StockService stockService;

    @RequestMapping("/add")
    public String add(){

        Stock stock =new Stock();
        stock.setProductId("9");
        stock.setCount(100);

        stockService.insert(stock);
        return "库存增加成功";
    }


    @RequestMapping("/reduct")
    public String reduct(@RequestParam(value = "productId") String productId){

        stockService.reduct(productId);
        return "扣减库存成功";
    }

}
