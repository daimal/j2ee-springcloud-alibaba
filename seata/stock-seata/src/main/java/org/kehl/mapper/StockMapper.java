package org.kehl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.kehl.entity.Stock;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Mapper
@Service
public interface StockMapper {
    int deleteByPrimaryKey(String id);

    int insert(Stock record);

    int insertSelective(Stock record);

    Stock selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Stock record);

    int updateByPrimaryKey(Stock record);

    int reduct(String productId);

}