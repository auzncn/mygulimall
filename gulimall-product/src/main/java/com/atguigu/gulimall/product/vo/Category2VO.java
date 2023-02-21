package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class Category2VO {
    private String catalog1Id;
    private List<Catalog3VO> catalog3List;
    private String id;
    private String name;

    @Data
    public static class Catalog3VO {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
