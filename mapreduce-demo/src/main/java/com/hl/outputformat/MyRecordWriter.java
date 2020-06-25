package com.hl.outputformat;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/**
 * 描述: 自定义输出
 * 作者: panhongtong
 * 创建时间: 2020-06-25 21:40
 **/
public class MyRecordWriter extends RecordWriter<LongWritable, Text> {

    private FSDataOutputStream hl;
    private FSDataOutputStream other;

    public void initialize(TaskAttemptContext taskAttemptContext) throws IOException {
        String output = taskAttemptContext.getConfiguration().get("mapreduce.output.fileoutputformat.outputdir");
        FileSystem fileSystem = FileSystem.get(taskAttemptContext.getConfiguration());
        hl = fileSystem.create(new Path(output + "/hl.log"));
        other = fileSystem.create(new Path(output + "/other.log"));
    }


    /**
     * 输出逻辑
     */
    @Override
    public void write(LongWritable longWritable, Text text) throws IOException, InterruptedException {
        String out = text.toString() + "\n";
        if (out.contains("hl")) {
            hl.write(out.getBytes());
        } else {
            other.write(out.getBytes());
        }
    }

    @Override
    public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        IOUtils.closeStream(hl);
        IOUtils.closeStream(other);
    }
}
