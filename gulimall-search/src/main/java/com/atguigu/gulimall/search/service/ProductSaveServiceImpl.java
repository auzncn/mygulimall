package com.atguigu.gulimall.search.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.conf.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsIndexConstant;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> models) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : models) {
            IndexRequest request = new IndexRequest(EsIndexConstant.PRODUCT_INDEX);
            request.id(String.valueOf(model.getSkuId()));
            String jsonString = JSONObject.toJSONString(model);
            request.source(jsonString, XContentType.JSON);
            bulkRequest.add(request);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(bulk.toString());
        boolean b = bulk.hasFailures();
        if (b) {

        }
        return !b;
    }
}
