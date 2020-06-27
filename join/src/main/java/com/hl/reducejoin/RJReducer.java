package com.hl.reducejoin;

import com.hl.bean.OrderBean;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

/**
 * 描述: 自定义Reducer
 * 作者: panhongtong
 * 创建时间: 2020-06-25 22:39
 **/
public class RJReducer extends Reducer<OrderBean, NullWritable, OrderBean, NullWritable> {

    @Override
    protected void reduce(OrderBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
        Iterator<NullWritable> iterator = values.iterator();
        // 读取第一行的pname
        iterator.next();
        String pname = key.getPname();
        // 将pname写入到order中
        while (iterator.hasNext()) {
            iterator.next();
            key.setPname(pname);
            context.write(key,NullWritable.get());
        }
    }
}
