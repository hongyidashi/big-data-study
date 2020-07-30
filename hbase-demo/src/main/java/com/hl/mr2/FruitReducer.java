package com.hl.mr2;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-07-30 15:55
 **/
public class FruitReducer extends TableReducer<ImmutableBytesWritable, Put, NullWritable> {

    @Override
    protected void reduce(ImmutableBytesWritable key, Iterable<Put> values, Context context) throws IOException, InterruptedException {

        // 遍历写出
        for (Put put : values) {
            context.write(NullWritable.get(), put);
        }
    }
}
