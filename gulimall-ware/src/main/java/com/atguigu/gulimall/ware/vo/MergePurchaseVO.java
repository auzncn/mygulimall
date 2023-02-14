package com.atguigu.gulimall.ware.vo;

import lombok.Data;

@Data
public class MergePurchaseVO {
    private Long purchaseId;
    private Long[] items;
}
