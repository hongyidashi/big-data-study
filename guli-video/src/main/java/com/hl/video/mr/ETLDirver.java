package com.hl.video.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 描述: 驱动类
 * 作者: panhongtong
 * 创建时间: 2020-07-06 17:59
 **/
public class ETLDirver implements Tool {

    private Configuration configuration;

    public static void main(String[] args) {
        Configuration configuration = new Configuration();

        try {
            int result = ToolRunner.run(configuration, new ETLDirver(), args);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        // 获取job对象
        Job job = Job.getInstance();

        // 设置jar包路径
        job.setJarByClass(ETLDirver.class);

        // 设置Mapper类和输出KV类型
        job.setMapperClass(ETLMapper.class);
        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);

        // 设置最终输出的KV类型
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        // 设置任务输入输出路径
//        FileInputFormat.setInputPaths(job, new Path("/Users/panhongtong/work/cahe/input"));
//        FileOutputFormat.setOutputPath(job, new Path("/Users/panhongtong/work/cahe/output"));
        FileInputFormat.setInputPaths(job, new Path(strings[0]));
        FileOutputFormat.setOutputPath(job, new Path(strings[1]));


        // 提交任务
        boolean result = job.waitForCompletion(true);

        return result ? 0 : 1;
    }

    @Override
    public void setConf(Configuration conf) {
        configuration = conf;
    }

    @Override
    public Configuration getConf() {
        return configuration;
    }
}
