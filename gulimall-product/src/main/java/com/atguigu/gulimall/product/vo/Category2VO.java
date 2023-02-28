package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class Category2VO {
    private Long catalog1Id;
    private List<Catalog3VO> catalog3List;
    private Long id;
    private String name;

    @Data
    public static class Catalog3VO {
        private Long catalog2Id;
        private Long id;
        private String name;
    }
}
