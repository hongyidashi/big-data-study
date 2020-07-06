package com.hl.mapjoin;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-06-27 13:44
 **/
public class MJMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

    /**
     * 用于存储缓存文件的map
     */
    private Map<String, String> PMAP = new HashMap<>();

    private Text k = new Text();

    /**
     * 将缓存文件读到pMap中
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // 获取缓存文件的path
        URI[] cacheFiles = context.getCacheFiles();
        String paht = cacheFiles[0].toString();

        // 开流
        BufferedReader bufferedReader = new BufferedReader(new FileReader(paht));

        // 读取
        String line;
        while (StringUtils.isNotEmpty(line = bufferedReader.readLine())) {
            String[] fields = line.split("\t");
            PMAP.put(fields[0], fields[1]);
        }

        IOUtils.closeStream(bufferedReader);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] fields = value.toString().split("\t");
        String pname = PMAP.get(fields[1]);
        k.set(fields[0] + "\t" + pname + "\t" + fields[2]);
        context.write(k, NullWritable.get());
    }
}
