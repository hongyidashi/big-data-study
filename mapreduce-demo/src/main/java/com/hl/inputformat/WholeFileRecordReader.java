package com.hl.inputformat;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

/**
 * 描述: 自定义RecordReader；处理一个文件；把这个文件直接读成KV值
 * 作者: panhongtong
 * 创建时间: 2020-06-22 16:52
 **/
public class WholeFileRecordReader extends RecordReader<Text, BytesWritable> {

    /**
     * 标志变量，记录是否已经读取
     */
    private boolean noRead = true;

    /**
     * key 和 value
     */
    private Text key = new Text();
    private BytesWritable value = new BytesWritable();

    /**
     * 输入流
     */
    private FSDataInputStream inputStream;

    /**
     * 文件切片
     */
    private FileSplit fileSplit;

    /**
     * 初始化方法
     */
    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        // 初始化输入流
        // 转换切片类型
        fileSplit = (FileSplit) inputSplit;
        // 通过切片获取path
        Path path = fileSplit.getPath();
        // 通过path获取文件系统
        FileSystem fileSystem = path.getFileSystem(taskAttemptContext.getConfiguration());
        // 通过文件系统开流
        inputStream = fileSystem.open(path);
    }

    /**
     * 尝试读取下一组kv值，如果读到了返回true，读完了返回false
     */
    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (noRead) {
            // 读取文件
            key.set(fileSplit.getPath().toString());
            byte[] buf = new byte[(int) fileSplit.getLength()];
            value.set(buf, 0, buf.length);

            noRead = false;
            return true;
        }
        return false;
    }

    /**
     * 获取当前读到的key
     */
    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    /**
     * 获取当前读到的value
     */
    @Override
    public BytesWritable getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    /**
     * 当前数据读取进度
     */
    @Override
    public float getProgress() throws IOException, InterruptedException {
        return noRead ? 0 : 1;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeStream(inputStream);
    }
}
