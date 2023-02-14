package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ????
 * 
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:46:27
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
