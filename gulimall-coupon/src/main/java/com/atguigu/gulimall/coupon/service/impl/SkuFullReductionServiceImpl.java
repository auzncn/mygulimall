package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Resource
    private SkuLadderService skuLadderService;
    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveInfo(SkuReductionTo to) {
        //阶梯价格
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(to, ladderEntity);
        ladderEntity.setAddOther(to.getCountStatus());
        skuLadderService.save(ladderEntity);

        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(to, fullReductionEntity);
        if (fullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
            this.save(fullReductionEntity);
        }
        //会员价格
        List<MemberPrice> memberPrice = to.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(e -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(to.getSkuId());
            memberPriceEntity.setMemberLevelId(e.getId());
            memberPriceEntity.setMemberLevelName(e.getName());
            memberPriceEntity.setMemberPrice(e.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(e -> e.getMemberPrice().compareTo(new BigDecimal(0)) > 0).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }
}