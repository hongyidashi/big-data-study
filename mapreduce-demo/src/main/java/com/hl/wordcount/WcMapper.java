package com.hl.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 描述: 映射
 * 作者: panhongtong
 * 创建时间: 2020-06-21 21:58
 * Mapper< 行首偏移量（即该行的位置）,内容类型, 输出类型, 输出类型 >
 **/
public class WcMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private Text word = new Text();
    private IntWritable one = new IntWritable(1);

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 拿到行数据
        String lineData = value.toString();

        // 以空格切分数据
        String[] words = lineData.split(" ");

        // 遍历数组，以自定义格式返回给context
        for (String word : words) {
            // 不直接new对象减少内存分配
            this.word.set(word);
            context.write(this.word, one);
        }
    }
}
