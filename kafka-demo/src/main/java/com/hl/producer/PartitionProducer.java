package com.hl.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 描述: 使用自定义分区器的生产者
 * 作者: panhongtong
 * 创建时间: 2020-07-17 14:49
 **/
public class PartitionProducer {
    public static void main(String[] args) {
        // 配置文件
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop103:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // 添加分区器
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.hl.partition.MyPartitioner");

        Producer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord<>("first", i+"","断腿少女-->" + i));
        }

        producer.close();
    }
}
