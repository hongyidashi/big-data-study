package com.hl.reducejoin;

import com.hl.bean.OrderBean;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-25 22:38
 **/
public class RJMapper extends Mapper<LongWritable, Text, OrderBean, NullWritable> {

    /**
     * 文件名
     */
    private String fileName;

    private OrderBean orderBean = new OrderBean();

    /**
     * 获取文件名
     * 每一个文件切片进来前会执行
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // 获取文件切片，通过切片获取文件名
        FileSplit fs = (FileSplit) context.getInputSplit();
        fileName = fs.getPath().getName();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fileds = value.toString().split("\t");
        // 通过文件名来判断输入并赋值
        if ("order.txt".equals(fileName)) {
            orderBean.setId(fileds[0]);
            orderBean.setPid(fileds[1]);
            orderBean.setAmount(Integer.parseInt(fileds[2]));
            orderBean.setPname("");
        } else {
            orderBean.setPid(fileds[0]);
            orderBean.setPname(fileds[1]);
            orderBean.setAmount(0);
            orderBean.setId("");
        }
        System.out.println("--->"+orderBean);
        context.write(orderBean, NullWritable.get());
    }
}
