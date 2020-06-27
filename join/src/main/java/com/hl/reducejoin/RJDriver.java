package com.hl.reducejoin;

import com.hl.bean.OrderBean;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-25 22:38
 **/
public class RJDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance();

        job.setJarByClass(RJDriver.class);

        job.setMapperClass(RJMapper.class);
        job.setReducerClass(RJReducer.class);

        job.setMapOutputKeyClass(OrderBean.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(OrderBean.class);
        job.setOutputValueClass(NullWritable.class);

        // 设置分组规则
        job.setGroupingComparatorClass(RJComparator.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/panhongtong/work/cahe/input"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/panhongtong/work/cahe/output"));

        boolean b = job.waitForCompletion(true);
        System.exit(b ? 200 : 500);
    }
}
