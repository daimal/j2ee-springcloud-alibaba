package org.kehl.service;

import org.kehl.entity.Stock;

public interface StockService {

    public String insert(Stock stock);

    void reduct(String productId);
}
