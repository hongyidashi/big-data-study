package com.hl.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 描述: 计算
 * 作者: panhongtong
 * 创建时间: 2020-06-21 21:58
 **/
public class WcReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable total = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        // 遍历values计算个数
        int sum = 0;

        for (IntWritable value : values) {
            sum += value.get();
        }

        total.set(sum);
        context.write(key, total);
    }
}
