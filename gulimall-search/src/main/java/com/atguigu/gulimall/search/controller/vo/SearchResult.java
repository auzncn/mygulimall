package com.atguigu.gulimall.search.controller.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    private List<SkuEsModel> product;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<BrandVO> brands;
    private List<CatalogVO> catalogs;
    private List<AttrVO> attrs;

    @Data
    public static class BrandVO {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVO {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVO {
        private Long attrId;
        private String attrName;

        private List<String> attrValue;
    }

}
