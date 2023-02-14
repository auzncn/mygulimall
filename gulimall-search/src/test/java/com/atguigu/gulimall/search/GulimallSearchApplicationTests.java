package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.search.conf.ElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Resource
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Test
    public void testIndex() throws IOException {
        User user = new User("bk-2", "City bike", 12);
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        String jsonString = JSONObject.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
    }

    @Test
    public void testSearch() throws IOException {

        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        searchSourceBuilder.aggregation(AggregationBuilders.terms("age_agg").field("age"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balance_agg").field("balance"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            Account account = JSONObject.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        Terms age_agg = searchResponse.getAggregations().get("age_agg");
        for (Terms.Bucket bucket : age_agg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄----->" + keyAsString + "人数------>" + bucket.getDocCount());
        }
        Avg balance_agg = searchResponse.getAggregations().get("balance_agg");
        System.out.println("平均薪资----->" + balance_agg.getValue());

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User {
        private String name;
        private String gender;
        private int age;
    }

    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

}
