package org.kehl.order.feign.Impl;

import org.kehl.order.feign.ProductFeignService;
import org.springframework.stereotype.Component;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-18 14:10
 **/
@Component
public class ProductFeignServiceFallbackImpl implements ProductFeignService {
    @Override
    public String getProduct() {
        return "降级了";
    }
}
