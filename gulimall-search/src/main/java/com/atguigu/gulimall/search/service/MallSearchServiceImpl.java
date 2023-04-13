package com.atguigu.gulimall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.conf.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsIndexConstant;
import com.atguigu.gulimall.search.controller.vo.SearchParam;
import com.atguigu.gulimall.search.controller.vo.SearchResult;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Resource
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult searchResult = null;
        try {
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            searchResult = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder searchSource = SearchSourceBuilder.searchSource();
        //查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        if (CollectionUtils.isNotEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        if (StringUtils.isNotBlank(param.getSkuPrice())) {
            String[] s = param.getSkuPrice().split("_");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (s.length == 1) {
                rangeQuery.gte(s[0]);
            } else if (s.length == 2) {
                if (StringUtils.isNotBlank(s[0])) {
                    rangeQuery.gte(s[0]);
                }
                rangeQuery.lte(s[1]);
            }
            boolQuery.filter(rangeQuery);
        }

        if (CollectionUtils.isNotEmpty(param.getAttrs())) {
            for (String attr : param.getAttrs()) {
                //attrs=1_5寸:8寸&2_16G:8G
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        searchSource.query(boolQuery);
        //排序
        if (StringUtils.isNotBlank(param.getSort())) {
            //sort=hotScore_asc/desc
            String[] s = param.getSort().split("_");
            SortOrder order = "asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            searchSource.sort(s[0], order);
        }
        //分页
        Integer pageNum = param.getPageNum();
        int pageSize = EsIndexConstant.PAGE_SIZE;
        searchSource.from((pageNum - 1) * pageSize);
        searchSource.size(pageSize);
        //高亮
        if (StringUtils.isNotBlank(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSource.highlighter(highlightBuilder);
        }

        //聚合
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(10);
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brandNameAgg").field("brandName").size(1);
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1);
        brandAgg.subAggregation(brandNameAgg);
        brandAgg.subAggregation(brandImgAgg);
        searchSource.aggregation(brandAgg);

        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg");
        catalogAgg.field("catalogId").size(20);
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1);
        catalogAgg.subAggregation(catalogNameAgg);
        searchSource.aggregation(catalogAgg);

        //属性聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrs", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg");
        attrIdAgg.field("attrs.attrId").size(20);
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1);
        attrIdAgg.subAggregation(attrNameAgg);
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(20);
        attrIdAgg.subAggregation(attrValueAgg);
        nested.subAggregation(attrIdAgg);
        searchSource.aggregation(nested);

        System.out.println(searchSource.toString());

        return new SearchRequest(new String[]{EsIndexConstant.PRODUCT_INDEX}, searchSource);
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        List<SkuEsModel> products = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            SkuEsModel skuEsModel = JSON.parseObject(hit.getSourceAsString(), SkuEsModel.class);

            if (StringUtils.isNotBlank(param.getKeyword())) {
                HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                skuEsModel.setSkuTitle(skuTitle.getFragments()[0].toString());
            }
            products.add(skuEsModel);
        }
        result.setProduct(products);
        result.setTotal(response.getHits().getTotalHits().value);
        long value = response.getHits().getTotalHits().value;
        Integer totalPage = (int) value % EsIndexConstant.PAGE_SIZE > 0 ? (int) value / EsIndexConstant.PAGE_SIZE + 1 : (int) value / EsIndexConstant.PAGE_SIZE;
        result.setTotalPages(totalPage);
        result.setPageNum(param.getPageNum());

        List<SearchResult.CatalogVO> catalogs = new ArrayList<>();
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalogAgg");
        catalogAgg.getBuckets().forEach(e -> {
            SearchResult.CatalogVO catalogVo = new SearchResult.CatalogVO();
            catalogVo.setCatalogId((Long) e.getKey());
            ParsedStringTerms catalogNameAgg = e.getAggregations().get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogs.add(catalogVo);
        });
        result.setCatalogs(catalogs);

        List<SearchResult.BrandVO> brands = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brandAgg");
        brandAgg.getBuckets().forEach(e -> {
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO();
            brandVO.setBrandId((Long) e.getKey());
            ParsedStringTerms brandNameAgg = e.getAggregations().get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandName(brandName);
            ParsedStringTerms brandImgAgg = e.getAggregations().get("brandImgAgg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandImg(brandImg);
            brands.add(brandVO);
        });
        result.setBrands(brands);

        List<SearchResult.AttrVO> attrs = new ArrayList<>();
        ParsedNested attrsAgg = response.getAggregations().get("attrs");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");

        attrIdAgg.getBuckets().forEach(a -> {
            SearchResult.AttrVO attrVO = new SearchResult.AttrVO();
            attrVO.setAttrId((Long) a.getKey());

            ParsedStringTerms attrNameAgg = a.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms attrValueAgg = a.getAggregations().get("attrValueAgg");
            List<String> values = attrValueAgg.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVO.setAttrName(attrName);
            attrVO.setAttrValue(values);
            attrs.add(attrVO);
        });

        result.setAttrs(attrs);
        return result;
    }

    public static void main(String[] args) {
        String s1 = "1_500";
        String s2 = "_500";
        String s3 = "500_";
//        System.out.println(Arrays.toString(s1.split("_")));
//        System.out.println((s1.split("_").length));
//        System.out.println(Arrays.toString(s2.split("_")));
//        System.out.println((s2.split("_").length));
//        System.out.println(Arrays.toString(s3.split("_")));
//        System.out.println((s3.split("_").length));
        String[] s = s2.split("_");
        System.out.println(StringUtils.isBlank(s[0]));
        System.out.println(StringUtils.isBlank(s[1]));

    }
}
