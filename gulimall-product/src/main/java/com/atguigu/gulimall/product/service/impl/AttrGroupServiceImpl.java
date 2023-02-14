package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.mybatis.QueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

//    @Override
//    public PageUtils queryPage(Map<String, Object> params) {
//        IPage<AttrGroupEntity> page = this.page(
//                new Query<AttrGroupEntity>().getPage(params),
//                new QueryWrapper<AttrGroupEntity>()
//        );
//        return new PageUtils(page);
//    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catId) {
        String key = (String) params.get("key");
        QueryWrapperX<AttrGroupEntity> wrapper = new QueryWrapperX<AttrGroupEntity>();
        if (catId != 0) {
            wrapper.eqIfPresent("catelog_id", catId);
        }
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> {
                w.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }
}