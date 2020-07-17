package com.hl.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 描述: 添加时间戳拦截器
 * 作者: panhongtong
 * 创建时间: 2020-07-17 16:56
 **/
public class TimeInterceptor implements ProducerInterceptor<String,String> {

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        // 因为传入的producerRecord不可修改，只能新创建一个对象返回
        return new ProducerRecord<>(producerRecord.topic(), producerRecord.partition(),
                System.currentTimeMillis() + producerRecord.key(), producerRecord.value());
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
