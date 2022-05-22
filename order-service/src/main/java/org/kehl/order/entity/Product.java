package org.kehl.order.entity;

import lombok.Data;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:21
 **/
@Data
public class Product {
    /**
     * 商品id
     */
    private String id;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品价格
     */
    private Double price;
    /**
     * 商品库存
     */
    private Integer stock;
}
