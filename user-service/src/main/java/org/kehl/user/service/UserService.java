package org.kehl.user.service;/**
 * @author by kehl
 * @date 2022/5/14.
 */

import org.kehl.user.entity.User;

/**
 * springcloud-alibaba
 * 用户服务
 * @author : kehl
 * @date : 2022-05-14 18:59
 **/
public interface UserService {
    public User getUserById(String id);

}
