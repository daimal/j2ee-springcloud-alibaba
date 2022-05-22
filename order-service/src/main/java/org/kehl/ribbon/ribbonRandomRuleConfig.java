package org.kehl.ribbon;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springcloud-alibaba
 * Ribbon随机负载均衡策略
 *
 * @author : kehl
 * @date : 2022-05-16 10:34
 **/
@Configuration
public class ribbonRandomRuleConfig
{
    @Bean
//    方法名一定要交iRule
    public IRule iRule(){
        return new RandomRule();
    }

}
