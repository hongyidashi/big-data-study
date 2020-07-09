package com.hl.source;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.PollableSource;

/**
 * 描述: 自定义source
 * 作者: panhongtong
 * 创建时间: 2020-07-09 11:49
 **/
public class MySource extends AbstractSource implements Configurable, PollableSource {

    /**
     * 前缀、后缀
     */
    private String PREFIX;
    private String SUFFIX;

    @Override
    public void configure(Context context) {
        PREFIX = context.getString("prefix");
        SUFFIX = context.getString("suffix", "-----over");
    }

    /**
     * 1. 接收数据（这里伪造）
     * 2. 封装为事件
     * 3. 将事件写入channel
     *
     * @return
     * @throws EventDeliveryException
     */
    @Override
    public Status process() throws EventDeliveryException {
        try {
            // 伪造5条数据
            for (int i = 0; i < 5; i++) {
                // 构建event对象
                Event event = new SimpleEvent();
                event.setBody((PREFIX + "----" + i + "----" + SUFFIX).getBytes());
                getChannelProcessor().processEvent(event);
            }
            Thread.sleep(5000);
            return Status.READY;
        } catch (Exception e) {
            e.printStackTrace();
            return Status.BACKOFF;
        }
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }
}
