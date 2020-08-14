package com.hl.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-08-10 12:26
 **/
public class TestClazz {
    public static void main(String[] args) {
        //下界
        List<? super Apple> flistBottem = new ArrayList<Apple>();
        flistBottem.add(new Apple());
        flistBottem.add(new Jonathan());
    }
}

class Fruit {}
class Apple extends Fruit {}
class Jonathan extends Apple {}
class Orange extends Fruit {}
