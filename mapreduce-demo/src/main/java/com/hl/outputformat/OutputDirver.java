package com.hl.outputformat;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-25 21:51
 **/
public class OutputDirver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Job job = Job.getInstance();

        job.setJarByClass(OutputDirver.class);

        job.setOutputFormatClass(MyOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path("/Users/panhongtong/work/cahe/input"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/panhongtong/work/cahe/output"));

        boolean b = job.waitForCompletion(true);
        System.exit(b ? 200 : 500);
    }
}
