package com.hl.mapjoin;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.net.URI;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-27 13:44
 **/
public class MJDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance();

        job.setMapperClass(MJMapper.class);
        job.setNumReduceTasks(0);

        // 设置缓存文件
        job.addCacheFile(URI.create("/Users/panhongtong/work/cahe/input/pd.txt"));

        FileInputFormat.setInputPaths(job, new Path("/Users/panhongtong/work/cahe/input/order.txt"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/panhongtong/work/cahe/output"));

        boolean b = job.waitForCompletion(true);
        System.exit(b ? 200 : 500);
    }
}
