package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVO {
    private Long id;
    private List<PurchaseItemVO> items;
}
