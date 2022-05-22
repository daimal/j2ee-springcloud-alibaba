package org.kehl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.kehl.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
@Service
public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    List<Order> selectAll();
}