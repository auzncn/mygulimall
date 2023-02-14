package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.mybatis.QueryWrapperX;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapperX<SkuInfoEntity> wrapper = new QueryWrapperX<>();
        wrapper.eqIfPresent("catalog_id", params.get("catalogId"))
                .eqIfPresent("brand_id", params.get("brandId"))
                .ge(params.get("min") != null && Integer.parseInt(params.get("min").toString()) > 0, "price", params.get("min"))
                .le(params.get("max") != null && Integer.parseInt(params.get("max").toString()) > 0, "price", params.get("max"))
                .and(StringUtils.isNotBlank(key), w -> {
                    w.eq("id", key).or().like("sku_name", params.get("skuName"));
                });
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}