package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyPaginationInnerInterceptor {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        // 这里就是分页插件的配置了，由于由@Configuration注解，所以是自动注入的，自动应用
        return new PaginationInterceptor();
    }


}
