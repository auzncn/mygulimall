package com.atguigu.gulimall.product.controller;

import java.util.*;
import java.util.stream.Collectors;


import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupAttrsVO;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:05:00
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrAttrgroupRelationService relationService;

    @Resource
    private AttrService attrService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        List<AttrAttrgroupRelationEntity> collect = vos.stream().map((v) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(v, entity);
            return entity;
        }).collect(Collectors.toList());
        relationService.saveBatch(collect);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R noattrList(@RequestParam Map<String, Object> params, @PathVariable Long attrgroupId) {
        AttrGroupEntity group = attrGroupService.getById(attrgroupId);
        Long catelogId = group.getCatelogId();

        List<AttrAttrgroupRelationEntity> relations = relationService.list();
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", catelogId);
        if (CollectionUtils.isNotEmpty(relations)) {
            List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(e -> e.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);

        return R.ok().put("page", new PageUtils(page));
    }

    @GetMapping("/{catId}/withattr")
    public R listAttrGroupWithAttrs(@PathVariable Long catId) {
        List<AttrGroupEntity> groups = attrGroupService.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catId));
        List<AttrGroupAttrsVO> data = groups.stream().map((g) -> {
            AttrGroupAttrsVO vo = new AttrGroupAttrsVO();
            BeanUtils.copyProperties(g, vo);
            List<AttrAttrgroupRelationEntity> relations = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_group_id", g.getAttrGroupId()));
            if (CollectionUtils.isNotEmpty(relations)) {
                List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                Collection<AttrEntity> attrEntities = attrService.listByIds(attrIds);
                List<AttrEntity> attrs = new ArrayList<>(attrEntities);
                vo.setAttrs(attrs);
            }
            return vo;
        }).collect(Collectors.toList());
        return R.ok().put("data", data);
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R relationList(@PathVariable Long attrgroupId) {
        List<AttrEntity> attrEntities = new ArrayList<>();
        List<AttrAttrgroupRelationEntity> relation = relationService
                .list(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_group_id", attrgroupId));
        if (!CollectionUtils.isEmpty(relation)) {
            List<Long> attrIds = relation.stream()
                    .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            attrEntities.addAll(attrService.listByIds(attrIds));
        }
        return R.ok().put("data", attrEntities);
    }

    @PostMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        relationService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catId) {
        PageUtils page = attrGroupService.queryPage(params, catId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        List<Long> path = categoryService.getPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
