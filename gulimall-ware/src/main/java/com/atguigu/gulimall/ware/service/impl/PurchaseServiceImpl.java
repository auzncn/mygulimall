package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.mybatis.QueryWrapperX;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergePurchaseVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.atguigu.gulimall.ware.vo.PurchaseItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Resource
    private PurchaseDetailService purchaseDetailService;
    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapperX<PurchaseEntity> wrapper = new QueryWrapperX<>();
        String key = (String) params.get("key");
        wrapper.eqIfPresent("status", params.get("status"));
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> {
                w.like("assignee_name", key).or()
                        .like("phone", key).or()
                        .like("ware_id", key);
            });
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode()).or()
                .eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(MergePurchaseVO vo) {
        Long purchaseId = vo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchase = new PurchaseEntity();
            purchase.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchase);
            purchaseId = purchase.getId();
        }
        List<PurchaseDetailEntity> detailList = new ArrayList<>();
        for (Long item : vo.getItems()) {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(item);
            purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            purchaseDetail.setPurchaseId(purchaseId);
            detailList.add(purchaseDetail);
        }
        purchaseDetailService.updateBatchById(detailList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void received(List<Long> ids) {
        List<PurchaseEntity> list = this.list(new QueryWrapper<PurchaseEntity>().in("id", ids)
                .and(w -> {
                    w.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode()).or()
                            .eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
                }));

        List<PurchaseEntity> collect = list.stream()
                .peek(e -> e.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode()))
                .collect(Collectors.toList());

        this.updateBatchById(collect);

        purchaseDetailService.update(new UpdateWrapper<PurchaseDetailEntity>()
                .set("status", WareConstant.PurchaseDetailStatusEnum.BUYING.getCode())
                .in("purchase_id", ids));

    }

    @Override
    public void done(PurchaseDoneVO vo) {
        PurchaseEntity purchase = this.getById(vo.getId());
        long failCount = vo.getItems().stream()
                .filter(e -> e.getStatus().equals(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()))
                .count();
        purchase.setStatus(failCount > 0 ? WareConstant.PurchaseStatusEnum.HASERROR.getCode() : WareConstant.PurchaseStatusEnum.FINISH.getCode());
        this.updateById(purchase);

        List<PurchaseDetailEntity> detailList = new ArrayList<>();
        for (PurchaseItemVO item : vo.getItems()) {
            PurchaseDetailEntity purchaseDetail = purchaseDetailService.getById(item.getItemId());
            purchaseDetail.setStatus(item.getStatus());
            detailList.add(purchaseDetail);
            if (item.getStatus().equals(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode())) {
                wareSkuService.addStock(purchaseDetail.getSkuId(), purchaseDetail.getWareId(), purchaseDetail.getSkuNum());
            }
        }
        purchaseDetailService.updateBatchById(detailList);

    }

}