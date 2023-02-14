package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:25:35
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
