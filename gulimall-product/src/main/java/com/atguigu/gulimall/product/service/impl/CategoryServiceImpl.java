package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree(Map<String, Object> params) {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        List<CategoryEntity> list = categoryEntities.stream().filter(category -> category.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, categoryEntities));
                    return menu;
                }).sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public void batchRemove(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public List<Long> getPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        getParentsPath(catelogId, list);
        Collections.reverse(list);
        return list;
    }

    private void getParentsPath(Long catelogId, List<Long> list) {
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        CategoryEntity father = baseMapper.selectById(categoryEntity.getParentCid());
        list.add(categoryEntity.getCatId());
        if (father != null) {
            getParentsPath(father.getCatId(), list);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity entity, List<CategoryEntity> category) {
        return category.stream().filter(e -> e.getParentCid().equals(entity.getCatId()))
                .map(e -> {
                    e.setChildren(getChildren(e, category));
                    return e;
                }).sorted(Comparator.comparingInt(m -> (m.getSort() == null ? 0 : m.getSort())))
                .collect(Collectors.toList());
    }
}