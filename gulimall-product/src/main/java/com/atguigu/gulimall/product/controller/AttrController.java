package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:05:00
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    /**
     * 修改spu规格
     */
    @RequestMapping("/update/{spuId}")
    public R updateBySpu(@PathVariable Long spuId, @RequestBody List<ProductAttrValueEntity> attrValues) {
        productAttrValueService.remove(new QueryWrapper<ProductAttrValueEntity>()
                .eq("spu_id", spuId));
        List<ProductAttrValueEntity> collect = attrValues.stream()
                .peek(e -> e.setSpuId(spuId))
                .collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);
        return R.ok();
    }

    /**
     * 获取spu规格
     */
    @RequestMapping("/base/listforspu/{spuId}")
    public R listforspu(@PathVariable Long spuId) {
        List<ProductAttrValueEntity> data = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>()
                .eq("spu_id", spuId));
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/{type}/list/{catId}")
    public R listByType(@RequestParam Map<String, Object> params, @PathVariable Long catId, @PathVariable String type) {
        PageUtils page = attrService.queryAttrPageType(params, catId, type);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrEntity attr = attrService.getById(attrId);
        AttrVO attrVO = new AttrVO();
        BeanUtils.copyProperties(attr, attrVO);

        List<Long> path = categoryService.getPath(attr.getCatelogId());
        attrVO.setCatelogPath(path);

        AttrAttrgroupRelationEntity relation = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_id", attrId));
        if (relation != null) {
            attrVO.setAttrGroupId(relation.getAttrGroupId());
        }
        return R.ok().put("attr", attrVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @Transactional
    public R save(@RequestBody AttrVO attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        attrService.save(entity);
        if (ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() == attr.getAttrType() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrGroupId(attr.getAttrGroupId());
            relation.setAttrId(entity.getAttrId());
            attrAttrgroupRelationService.save(relation);
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVO attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        attrService.updateById(entity);
        if (attr.getAttrGroupId() != null) {
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attr.getAttrId()));
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            relation.setAttrId(attr.getAttrId());
            relation.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationService.save(relation);
        }
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
