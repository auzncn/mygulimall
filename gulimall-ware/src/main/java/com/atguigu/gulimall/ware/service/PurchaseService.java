package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MergePurchaseVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:52:27
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void merge(MergePurchaseVO vo);

    void received(List<Long> ids);

    void done(PurchaseDoneVO vo);
}

