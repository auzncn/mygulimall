package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.mybatis.QueryWrapperX;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.spuInfo.Images;
import com.atguigu.gulimall.product.vo.spuInfo.SpuSaveVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("spuInfoService")
@Transactional(rollbackFor = Exception.class)
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private AttrService attrService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapperX<SpuInfoEntity> wrapper = new QueryWrapperX<>();
        String key = (String) params.get("key");
        wrapper.eqIfPresent("brand_id", params.get("brandId"))
                .eqIfPresent("catalog_id", params.get("catalogId"))
                .eqIfPresent("publish_status",params.get("status"))
                .and(StringUtils.isNotBlank(key), w -> {
                    w.eq("id", key).or().like("spu_name", key);
                });
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void spuUp(Long spuId) {

    }

    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //保存spu基本信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //保存描述
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(StringUtils.join(vo.getDecript(), ","));
        spuInfoDescService.save(descEntity);

        //保存spu图片集
        if (CollectionUtils.isNotEmpty(vo.getImages())) {
            List<SpuImagesEntity> collect = vo.getImages().stream().map(e -> {
                SpuImagesEntity imagesEntity = new SpuImagesEntity();
                imagesEntity.setSpuId(spuId);
                imagesEntity.setImgUrl(e);
                return imagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(collect);
        }

        //保存spu规格参数
        if (CollectionUtils.isNotEmpty(vo.getBaseAttrs())) {
            List<ProductAttrValueEntity> collect = vo.getBaseAttrs().stream().map(e -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setAttrId(e.getAttrId());
                AttrEntity byId = attrService.getById(e.getAttrId());
                productAttrValueEntity.setAttrName(byId.getAttrName());
                productAttrValueEntity.setAttrValue(e.getAttrValues());
                productAttrValueEntity.setQuickShow(e.getShowDesc());
                productAttrValueEntity.setSpuId(spuId);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(collect);
        }
        //积分信息
        SpuBoundTo to = new SpuBoundTo();
        BeanUtils.copyProperties(vo.getBounds(), to);
        to.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(to);
        if (r.getCode() != 0) {
            log.error("远程保存积分信息失败");
        }

        if (CollectionUtils.isNotEmpty(vo.getSkus())) {
            vo.getSkus().forEach(e -> {
                //保存sku信息
                String skuDefaultImg = "";
                Optional<Images> any = e.getImages().stream().filter(i -> i.getDefaultImg() == 1).findAny();
                if (any.isPresent()) {
                    skuDefaultImg = any.get().getImgUrl();
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(e, skuInfoEntity);
                skuInfoEntity.setBrandId(vo.getBrandId());
                skuInfoEntity.setCatalogId(vo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(skuDefaultImg);
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                //sku图片信息
                List<SkuImagesEntity> skuImagesEntities = e.getImages().stream().map(si -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(si, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter(si -> StringUtils.isNotBlank(si.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //sku销售属性
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = e.getAttr().stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //sku优惠信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(e, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存优惠信息失败");
                    }
                }
            });

        }

    }


}