package org.kehl.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * springcloud-alibaba
 * 自定义的负载均衡策略
 *
 * @author : kehl
 * @date : 2022-05-16 14:19
 **/
public class CustomRule extends AbstractLoadBalancerRule {

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer loadBalancer=this.getLoadBalancer();
        List<Server> reachableServers =loadBalancer.getReachableServers();

        int random= ThreadLocalRandom.current().nextInt(reachableServers.size());
        Server server = reachableServers.get(random);
        //这个可以不写，因为getReachableServers()只会返回可用的服务给我们
//        if (!server.isAlive()){
//            return null;
//        }

        return server;

    }
}
