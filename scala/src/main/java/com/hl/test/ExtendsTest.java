package com.hl.test;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-08-06 15:16
 **/
public class ExtendsTest {
    public static void main(String[] args) {
        Sub clazz1 = new Sub();
        System.out.println(clazz1.name);

        Supper clazz2 = new Sub();
        System.out.println(clazz2.name);

        Sub clazz3 = (Sub) clazz2;
        System.out.println(clazz3.name);
    }
}

class Supper {
    String name = "大福";
}

class Sub extends Supper {
    String name = "番茄炒鳄鱼";
}
