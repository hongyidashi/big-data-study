package com.hl.func.udtf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述: 自定义UDTF 聚集函数 - 分割字符串
 * 作者: panhongtong
 * 创建时间: 2020-07-06 10:36
 **/
public class MyUDTF extends GenericUDTF {

    private List<String> DATALIST = new ArrayList();

    /**
     * 初始化方法，一定要实现，默认实现会直接抛出异常
     */
    @Override
    public StructObjectInspector initialize(StructObjectInspector argOIs) throws UDFArgumentException {
        // 定义输出数据的列名
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("word");

        // 定义输出数据的类型
        List<ObjectInspector> fieldOIs = new ArrayList<>();
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    @Override
    public void process(Object[] objects) throws HiveException {
        // 拿到参数
        // 要处理的数据
        String data = objects[0].toString();
        // 分隔符
        String splitKey = objects[1].toString();

        // 分割
        String[] words = data.split(splitKey);

        // 遍历输出
        for (String word : words) {
            DATALIST.clear();
            DATALIST.add(word);
            forward(DATALIST);
        }

    }

    @Override
    public void close() throws HiveException {

    }
}
