package com.example.elasticsearch.controller;

import com.example.elasticsearch.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-09-29 10:16
 **/
@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        return contentService.parseContent(keyword);
    }

    @GetMapping("search/{pageNo}/{pageSize}/{keyword}")
    public List<Map<String, Object>> search(@PathVariable String keyword,
                                            @PathVariable int pageNo,
                                            @PathVariable int pageSize) throws IOException {
        return contentService.searchPage(keyword, pageNo, pageSize);
    }

}
