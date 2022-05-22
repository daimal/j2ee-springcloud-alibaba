package org.kehl.order.interceptor.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * springcloud-alibaba
 * 自定义feign拦截器
 *
 * @author : kehl
 * @date : 2022-05-16 17:21
 **/
public class CustomFeignInterceptor implements RequestInterceptor {
    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String url=requestTemplate.url();
        logger.info(url);
//        将含有getUser的路由的id改为9
        if (url.contains("/getUser"))
            url=url.replace(url.charAt(url.length()-1)+"","9");
            requestTemplate.uri(url);
//        requestTemplate.header("xxx","xxx");
//        requestTemplate.query("id","111");
//        requestTemplate.uri("/9");
        logger.info("feign拦截器!");
    }
}
