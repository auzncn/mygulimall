package com.atguigu.gulimall.search.conf;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    @Bean
    public RestHighLevelClient client() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("192.168.222.128", 9200, "http"));
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        System.out.println(restHighLevelClient);
        return restHighLevelClient;
    }

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }
}
