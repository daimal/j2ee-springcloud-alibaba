package org.kehl.user.dao;/**
 * @author by kehl
 * @date 2022/5/14.
 */

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.kehl.user.entity.User;
import org.springframework.stereotype.Repository;

/**
 * <h4>springcloud-alibaba</h4>
 * <p>用户实体</p>
 * @author : kehl
 * @date : 2022-05-14 18:54
 **/
@Slf4j
@Repository
public class UserDao {
    public User getUserById(String id){
        User user=new User();
        user.setId(id);
        user.setName(id+" 张三");
        log.info("用户信息，查询结果："+ JSON.toJSONString(user));
        return user;
    }
}
