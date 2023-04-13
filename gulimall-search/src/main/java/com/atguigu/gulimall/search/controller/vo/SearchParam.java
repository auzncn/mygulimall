package com.atguigu.gulimall.search.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;
    private Long  catalog3Id;
    private List<Long> brandId;
    /**
     *price/salecount/hotscore
     * asc/desc
     * price_asc
     */
    private String sort;

    /**
     * 1_500,_500
     */
    private String skuPrice;

    private Integer hasStock;

    private List<String> attrs;

    private Integer pageNum = 1;

}
