package com.atguigu.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("search")
public interface SearchFeignService {

}
