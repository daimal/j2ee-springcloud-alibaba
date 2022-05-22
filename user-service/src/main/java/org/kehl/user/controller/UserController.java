package org.kehl.user.controller;

import org.kehl.user.entity.User;
import org.kehl.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * springcloud-alibaba
 * 用户控制层
 *
 * @author : kehl
 * @date : 2022-05-14 19:07
 **/
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Value("${server.port}")
    String port;

    @RequestMapping("/getUser/{Id}")
    public String getUser(@PathVariable("Id") String Id){
        return "success"+Id+" "+port;
    }

    @RequestMapping("/getUserInfo/{id}")
    public User getUserInfo (@PathVariable("id") String id){
        return userService.getUserById(id);
    }

}
