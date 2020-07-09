package com.hl.interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述: 自定义拦截器
 * 作者: panhongtong
 * 创建时间: 2020-07-09 09:51
 **/
public class TypeInterceptor implements Interceptor {

    private List<Event> EVENTLIST;

    @Override
    public void initialize() {
        EVENTLIST = new ArrayList<>();
    }

    @Override
    public Event intercept(Event event) {
        // 获取头信息
        Map<String, String> headers = event.getHeaders();

        // 获取事件中的body信息
        String body = new String(event.getBody());

        // 根据body中是否含有hello来确定添加什么头信息
        if (body.contains("hello")) {
            headers.put("type", "hl");
        } else {
            headers.put("type", "fawa1988");
        }
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> list) {
        EVENTLIST.clear();
        list.stream().map(event -> EVENTLIST.add(intercept(event)));
        return EVENTLIST;
    }

    @Override
    public void close() {

    }

    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new TypeInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
