package com.hl.mr1;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 描述: 主程序
 * 作者: panhongtong
 * 创建时间: 2020-07-30 14:15
 **/
public class FruitDriver implements Tool {

    private Configuration cof = null;

    @Override
    public int run(String[] args) throws Exception {
        // 获取job
        Job job = Job.getInstance();

        // 设置类路径
        job.setJarByClass(FruitDriver.class);

        // 设置map
//        job.setMapperClass(FruitMapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // 设置reducer
        TableMapReduceUtil.initTableReducerJob(args[1], FruitReducer.class, job);

        // 设置输入参数
        FileInputFormat.setInputPaths(job, new Path(args[0]));

        // 提交job
        boolean result = job.waitForCompletion(true);

        return result ? 0 : 1;
    }

    @Override
    public void setConf(Configuration configuration) {
        cof = configuration;
    }

    @Override
    public Configuration getConf() {
        return cof;
    }

    public static void main(String[] args) {

        try {
            Configuration configuration = new Configuration();
            int run = ToolRunner.run(configuration, new FruitDriver(), args);
            System.exit(run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
