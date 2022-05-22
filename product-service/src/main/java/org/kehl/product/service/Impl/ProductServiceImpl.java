package org.kehl.product.service.Impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kehl.product.dao.ProductDao;
import org.kehl.product.entity.Product;
import org.kehl.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:15
 **/
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Override
    public Product getProductById(String id){
        log.info("8081获取商品id为"+id+"的商品信息");
        Product product = productDao.getProductById(id);
        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
        return product;
    }

    @Override
    public String getProduct() {
        return "查询产品";
    }

}
