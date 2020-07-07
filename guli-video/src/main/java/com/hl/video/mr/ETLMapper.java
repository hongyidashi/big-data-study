package com.hl.video.mr;

import com.hl.video.utils.ETLUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-07-06 17:59
 **/
public class ETLMapper extends Mapper<LongWritable, Text, NullWritable, Text> {

    /**
     * 输出类型变量
     */
    private Text v = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 获取数据
        String oriStr = value.toString();

        // 过滤数据
        String etlStr = ETLUtil.etlStr(oriStr);

        if (StringUtils.isEmpty(etlStr)) {
            return;
        }

        // 写出数据
        v.set(etlStr);
        context.write(NullWritable.get(), v);
    }
}
