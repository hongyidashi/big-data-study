package com.hl.mr1;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * 描述: 将数据输入到HBase
 * 作者: panhongtong
 * 创建时间: 2020-07-30 13:56
 **/
public class FruitReducer extends TableReducer<LongWritable, Text, NullWritable> {

    @Override
    protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // 遍历
        for (Text value : values) {

            // 切割
            String[] fields = value.toString().split("\t");

            // 构建put对象并赋值
            Put put = new Put(Bytes.toBytes(fields[0]));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes(fields[1]));
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("color"), Bytes.toBytes(fields[2]));

            // 写入context
            context.write(NullWritable.get(), put);
        }
    }
}
