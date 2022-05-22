package org.kehl.order.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "user-service",path = "/user")
public interface UserFeignService {

    @RequestMapping("/getUser/{Id}")
    public String getUser(@PathVariable("Id") String Id);

}
