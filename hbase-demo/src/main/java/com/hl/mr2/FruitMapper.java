package com.hl.mr2;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-07-30 15:46
 **/
public class FruitMapper extends TableMapper<ImmutableBytesWritable, Put> {

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        // 构建put对象
        Put put = new Put(key.get());

        // 读取数据
        for (Cell cell : value.rawCells()) {
            // 判断当前cell是否为name列
            if ("name".equals(Bytes.toString(CellUtil.cloneQualifier(cell)))) {
                // 给put对象赋值
                put.add(cell);
            }

            // 写出
            context.write(key, put);
        }
    }
}
