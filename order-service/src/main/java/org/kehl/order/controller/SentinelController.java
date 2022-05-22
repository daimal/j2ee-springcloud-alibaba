package org.kehl.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-14 19:53
 **/
@Slf4j
@RestController
@RequestMapping("/sentinel")
public class SentinelController {
    @RequestMapping("/message1")
    public String message1(){
        return "范问sentinel第一信息";
    }

    @RequestMapping("/message3")
    public String message3(){
        return "范问sentinel第二信息";
    }
}
