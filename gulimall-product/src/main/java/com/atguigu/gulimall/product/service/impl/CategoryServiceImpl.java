package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.vo.Category2VO;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //    private HashMap<String, Object> cache = new HashMap<>();
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> level1Categorys = this.list(new QueryWrapper<CategoryEntity>()
                .eq("parent_cid", 0));
        return level1Categorys;
    }


    public Map<String, List<Category2VO>> getCatalogJsonDb() {
        synchronized (this) {
//            Map<String, List<Category2VO>> catalogJson = (Map<String, List<Category2VO>>) cache.get("catalogJson");
            String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
            if (StringUtils.isBlank(catalogJson)) {
                System.out.println("进入数据库查询。。。。。。。。。。。。。。。。。。。。。。");
                List<CategoryEntity> selectList = this.list();
                List<CategoryEntity> level1Categorys1 = getParentCid(selectList, 0L);
                Map<String, List<Category2VO>> map = level1Categorys1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    List<CategoryEntity> level2 = getParentCid(selectList, v.getCatId());
                    List<Category2VO> category2VOS = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(level2)) {
                        category2VOS = level2.stream().map(e -> {
                            Category2VO category2VO = new Category2VO();
                            category2VO.setCatalog1Id(v.getCatId());
                            category2VO.setId(e.getCatId());
                            category2VO.setName(e.getName());
                            List<CategoryEntity> level3 = getParentCid(selectList, e.getCatId());
                            List<Category2VO.Catalog3VO> collect = level3.stream().map(l3 -> {
                                Category2VO.Catalog3VO catalog3VO = new Category2VO.Catalog3VO();
                                catalog3VO.setId(l3.getCatId());
                                catalog3VO.setCatalog2Id(e.getCatId());
                                catalog3VO.setName(l3.getName());
                                return catalog3VO;
                            }).collect(Collectors.toList());
                            category2VO.setCatalog3List(collect);
                            return category2VO;
                        }).collect(Collectors.toList());
                    }
                    return category2VOS;
                }));
                String jsonString = JSON.toJSONString(map);
                stringRedisTemplate.opsForValue().set("catalogJson", jsonString);
                return map;
            }
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Category2VO>>>() {});
        }
    }

    @Override
    public Map<String, List<Category2VO>> getCatalogJson() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isBlank(catalogJson)) {
            System.out.println("缓存中没有数据，开始查找数据库。。。。。。。。。。。。。。。。。。。");
            return getCatalogJsonDb();
        }
        System.out.println("缓存中有数据，直接返回。。。。。。。。。。。。。。。。。。。");
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Category2VO>>>() {});
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> list, Long parentCid) {
        return list.stream().filter(e -> e.getParentCid().equals(parentCid)).collect(Collectors.toList());
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