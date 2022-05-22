package org.kehl.product.dao;

import org.kehl.product.entity.Product;
import org.springframework.stereotype.Repository;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:10
 **/
@Repository
public class ProductDao {
    public Product getProductById(String id){
        Product product = new Product();
        product.setId(id);
        product.setName("商品_"+id);
        product.setPrice(1 + Double.valueOf(id));
        product.setStock(100);
        return product;
    }
}
