package com.hl.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 描述: 普通生产者
 * 作者: panhongtong
 * 创建时间: 2020-07-17 11:41
 **/
public class MyProducer {
    public static void main(String[] args) {
        // kafka配置文件
        Properties props = new Properties();

        //kafka 集群，broker-list
        props.put("bootstrap.servers", "hadoop103:9092");
        // 设置应答模式
        props.put("acks", "all");
        //重试次数
        props.put("retries", 1);
        //批次大小
        props.put("batch.size", 16384);
        //等待时间
        props.put("linger.ms", 1);
        //RecordAccumulator 缓冲区大小
        props.put("buffer.memory", 33554432);
        // 设置序列化器
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 创建生产者对象
        Producer<String, String> producer = new KafkaProducer<>(props);

        // 发送消息
        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord<>("first", "断腿少女-" + i));
        }

        // 关闭资源
        producer.close();

    }
}
