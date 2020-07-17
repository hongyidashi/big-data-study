package com.hl.partition;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 描述: 自定义分区
 * 作者: panhongtong
 * 创建时间: 2020-07-17 14:42
 **/
public class MyPartitioner implements Partitioner {
    @Override
    public int partition(String s, Object o, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
        // 获取指定topic的可用分区数
        Integer countPartition = cluster.partitionCountForTopic("first");
        int partition;
        if (o == null) {
            partition = s.hashCode() % countPartition;
        } else {
            partition = o.hashCode() % countPartition;
        }
        System.out.println("自定义分区器被调用，分区为："+ partition);
        return partition;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
