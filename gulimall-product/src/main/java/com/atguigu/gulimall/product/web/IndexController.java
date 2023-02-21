package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Category2VO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
//@RequestMapping("product/attr")
public class IndexController {
    @Resource
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String getIndexPage(Model model) {
        List<CategoryEntity> list = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", list);
        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Category2VO>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }
}
