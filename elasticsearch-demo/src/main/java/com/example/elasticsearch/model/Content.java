package com.example.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: 爬取的商品数据的对象
 * 作者: panhongtong
 * 创建时间: 2020-09-29 10:00
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {

    private String title;
    private String img;
    private String price;

}
