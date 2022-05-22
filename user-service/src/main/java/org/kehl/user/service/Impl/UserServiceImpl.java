package org.kehl.user.service.Impl;

import org.kehl.user.dao.UserDao;
import org.kehl.user.entity.User;
import org.kehl.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * springcloud-alibaba
 * 用户服务实现
 * @author : kehl
 * @date : 2022-05-14 19:03
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;

    @Override
    public User getUserById(String id){
        return userDao.getUserById(id);
    }
}
