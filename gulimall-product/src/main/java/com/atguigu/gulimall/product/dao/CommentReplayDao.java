package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:05:00
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
