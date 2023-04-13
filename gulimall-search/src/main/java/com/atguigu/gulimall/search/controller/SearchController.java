package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.controller.vo.SearchParam;
import com.atguigu.gulimall.search.controller.vo.SearchResult;
import com.atguigu.gulimall.search.service.MallSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

@Controller
public class SearchController {
    @Resource
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String list(SearchParam param, Model model){
        SearchResult result =  mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
