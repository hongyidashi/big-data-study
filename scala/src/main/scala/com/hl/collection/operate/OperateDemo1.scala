package com.hl.collection.operate

/**
 * 描述: 操作
 * 作者: panhongtong
 * 创建时间: 2020-08-11 08:47
 **/
object OperateDemo1 {
  def main(args: Array[String]): Unit = {
    val list1 = List(1, 2, 3, 4)
    val list2 = list1.map(multiple)
    println(list2)

    val list3 = List("fuck", "shit", "GDX")
    val list4 = list3.map(upper)
    println(list4)
    val list5 = list3.flatMap(upper)
    println(list5)

    val list6 = list3.filter(startW)
    println(list6)

    val sumNum = list1.reduceLeft(sum)
    println(sumNum)

    val list7 = List(5, 3, 9, 0, 1, -3)
    println(list7.reduceLeft(maxNum))

    // 折叠
    println("=======================")
    println(list7.foldLeft(20)(minus))
    println(list7.foldRight(20)(minus))

    val list8 = List(1, 2)
    println((5 /: list1)(minus))
  }

  def minus(n1: Int, n2: Int): Int = {
    n1 - n2
  }

  def maxNum(n1: Int, n2: Int): Int = {
    if (n1 > n2) n1 else n2
  }

  def sum(n1: Int, n2: Int): Int = {
    n1 + n2
  }

  def startW(str: String): Boolean = {
    str.startsWith("s")
  }

  def upper(str: String): String = {
    str.toUpperCase
  }

  def multiple(n: Int): Int = {
    n << 1
  }
}