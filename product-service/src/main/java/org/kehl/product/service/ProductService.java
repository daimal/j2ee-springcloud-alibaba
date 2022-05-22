package org.kehl.product.service;

import org.kehl.product.entity.Product;

public interface ProductService {
    public Product getProductById(String Id);
    public String getProduct();
}
