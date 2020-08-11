package com.hl.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 描述: map
 * 作者: panhongtong
 * 创建时间: 2020-08-10 10:32
 **/
public class MapDemo {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap();
        System.out.println(map.get("k"));
        map.put(null, "傻了吧");
        System.out.println(map.get(null));

        Set<String> set = new HashSet<>();
        set.add(null);
        System.out.println(set);
    }
}
