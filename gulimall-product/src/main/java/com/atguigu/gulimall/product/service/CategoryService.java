package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Category2VO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:05:00
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree(Map<String, Object> params);

    void batchRemove(List<Long> ids);

    List<Long> getPath(Long catelogId);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Category2VO>> getCatalogJson();


}

