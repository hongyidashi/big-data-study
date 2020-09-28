package com.example.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-09-28 15:16
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dog {

    private String name;
    private Integer age;

}
