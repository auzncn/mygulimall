package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.controller.vo.SearchParam;
import com.atguigu.gulimall.search.controller.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam param);
}
