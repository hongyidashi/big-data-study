package com.hl.collection.list

/**
 * 描述: list
 * 作者: panhongtong
 * 创建时间: 2020-08-09 09:28
 **/
object ListDemo {
  def main(args: Array[String]): Unit = {
    val list01 = List(1, "大福", 3)
    println(list01)

    val list02 = Nil
    println(list02)

    println(list01(1))

    for (elem <- list01) {
      println(elem)
    }

    val list03 = list01.appended("断腿少女")
    println(list03)
    val list04 = list03.prepended("在前面？")
    println(list04)

    val list05 = list04 :+ "还能这么追加？"
    println(list05)
    val list06 = "操，牛逼啊" +: list05
    println(list06)

    val list07 = 2 :: 4 :: 2 :: list06 :: Nil
    println(list07)

    // 将集合的每一个元素加到一个空集合
    val list08 = "牛逼" :: 9 :: list07 ::: Nil
    println(list08)
  }
}
