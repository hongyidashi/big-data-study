package com.hl.outputformat;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 描述: 自定义输出格式
 * 作者: panhongtong
 * 创建时间: 2020-06-25 21:45
 **/
public class MyOutputFormat extends FileOutputFormat {
    @Override
    public RecordWriter getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        MyRecordWriter recordWriter = new MyRecordWriter();
        recordWriter.initialize(taskAttemptContext);
        return recordWriter;
    }
}
