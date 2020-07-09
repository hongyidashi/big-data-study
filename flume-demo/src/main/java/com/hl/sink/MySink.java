package com.hl.sink;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-07-09 14:52
 **/
public class MySink extends AbstractSink implements Configurable {

    /**
     * 日志对象
     */
    private Logger LOGGER = LoggerFactory.getLogger(MySink.class);

    /**
     * 前缀、后缀
     */
    private String PREFIX;
    private String SUFFIX;

    @Override
    public void configure(Context context) {
        PREFIX = context.getString("prefix");
        SUFFIX = context.getString("suffix", "over");
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status;

        // 获取绑定的channel
        Channel channel = getChannel();

        // 获取channel上的事务
        Transaction transaction = channel.getTransaction();

        // 开启事务
        transaction.begin();
        try {
            // 获取事件
            Event event = channel.take();

            if (event != null) {
                // 业务操作
                String body = new String(event.getBody());
                LOGGER.info(PREFIX+body+SUFFIX);
            }

            // 提交
            transaction.commit();

            status = Status.READY;
        } catch (ChannelException e) {
            e.printStackTrace();
            transaction.rollback();
            status = Status.BACKOFF;
        } finally {
            transaction.close();
        }
        return status;
    }

}
