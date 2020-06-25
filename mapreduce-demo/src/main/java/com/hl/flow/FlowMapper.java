package com.hl.flow;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-22 09:53
 **/
public class FlowMapper extends Mapper<LongWritable, Text, Text, FlowBean> {

    private Text phone = new Text();
    private FlowBean flow = new FlowBean();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");
        // 拿到电话号码
        phone.set(fields[1]);
        // 设置流量
        flow.set(Long.parseLong(fields[fields.length - 3]), Long.parseLong(fields[fields.length - 2]));
        context.write(phone, flow);
    }
}
