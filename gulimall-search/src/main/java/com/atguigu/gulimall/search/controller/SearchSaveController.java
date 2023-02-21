package com.atguigu.gulimall.search.controller;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("search/save")
public class SearchSaveController {

    @Resource
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R<Boolean> productStatusUp(@RequestBody List<SkuEsModel> models) {
        Boolean b = false;
        try {
            b = productSaveService.productStatusUp(models);
        } catch (IOException e) {
            e.printStackTrace();
        }
        R ok = R.ok();
        ok.setData(b);
        return ok;
    }
}
