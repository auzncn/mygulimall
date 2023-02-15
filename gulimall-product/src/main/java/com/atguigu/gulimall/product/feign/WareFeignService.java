package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("ware")
public interface WareFeignService {

    @PostMapping(value = "/ware/waresku/hasStock")
    R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds);

}
