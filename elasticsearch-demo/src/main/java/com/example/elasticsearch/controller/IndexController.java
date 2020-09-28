package com.example.elasticsearch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 描述: 首页控制器
 * 作者: panhongtong
 * 创建时间: 2020-09-28 17:30
 **/
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
