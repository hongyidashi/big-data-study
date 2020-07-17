package com.hl.producer;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * 描述: 回调生产者
 * 作者: panhongtong
 * 创建时间: 2020-07-17 14:10
 **/
public class CallbackProducer {
    public static void main(String[] args) {
        // 配置文件
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop103:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord<>("first", "断腿少女-->" + i),(metadata,ex)-> {
                if (ex == null) {
                    System.out.println(metadata.partition()+"--->"+metadata.offset());
                }
            });
        }

        producer.close();
    }
}
