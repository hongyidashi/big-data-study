package com.hl.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-07-17 17:28
 **/
public class CounterInterceptor implements ProducerInterceptor<String,String> {

    /**
     * 成功、失败次数
     */
    private AtomicInteger sucessCount = new AtomicInteger();
    private AtomicInteger failCount = new AtomicInteger();

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        if (recordMetadata != null) {
            sucessCount.incrementAndGet();
        } else {
            failCount.incrementAndGet();
        }
    }

    @Override
    public void close() {
        System.out.println("成功次数：" + sucessCount);
        System.out.println("失败次数：" + failCount);
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
