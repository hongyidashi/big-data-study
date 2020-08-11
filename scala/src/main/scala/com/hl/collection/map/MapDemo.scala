package com.hl.collection.map

import scala.collection.mutable

/**
 * 描述: map
 * 作者: panhongtong
 * 创建时间: 2020-08-10 10:21
 **/
object MapDemo {
  def main(args: Array[String]): Unit = {
    // 不可变
    val map1 = Map("k1" -> 10, "k2" -> 20, "k3" -> "大福")
    println(map1)
    println(map1.get("k3"))

    // 可变
    val map2 = mutable.Map("k1" -> 10, "k2" -> 20, "k3" -> "大福")
    map2.addOne("k4","大傻子")
    println(map2.get("k5"))
    //println(map2("k5"))  // 这样子会出异常
    //map2 ++ ("k5","GDX")
    println(map2.get("k4").get)

    println(map2.getOrElse("k5","GDX"))

    map2("k5") = "狗东西"
    println(map2)
    map2.put("k5","你是真的狗")
    println(map2)
    map2 += ("k6" -> "弟弟")
    println(map2)
    map2 subtractAll List("k6","k5")
    println(map2)

    for (elem <- map2) {
      println(elem)
    }

    for (elem <- map2.keys) {
      println(elem)
    }

    for (elem <- map2.values) {
      println(elem)
    }
    println("------------")

    for ((k,v) <- map2) {
      println(k + "=" + v)
    }
  }
}
