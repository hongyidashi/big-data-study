package com.hl.func.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 描述: 自定义UDF函数 一进一出
 * 作者: panhongtong
 * 创建时间: 2020-07-06 10:26
 **/
public class MyUDF extends UDF {

    public int evaluate(Integer num) {
        return num+5;
    }

}
