package com.hl.collection.operate

/**
 * 描述: 集合操作
 * 作者: panhongtong
 * 创建时间: 2020-08-11 09:41
 **/
object OperateDemo2 {
  def main(args: Array[String]): Unit = {
    val list1 = List(1, 2, 3, 4)
    println(list1.scanLeft(5)(minus))

    val list2 = List(5, 6, 7, 8)
    println(list1.zip(list2))
    val list3 = List(5, 6, 7, 8, 9)
    println(list1.zip(list3))

    val stream1 = numsForm(1)
    println(stream1)
    println(stream1.head)
    println(stream1.tail)

    val view = (1 to 100).view.filter(eq)
    println(view)
    println(view.head)
    println(view.foreach(elem => println(elem)))
  }

  def eq(n: Int): Boolean = {
    n.toString.equals(n.toString.reverse)
  }

  def numsForm(n: BigInt): Stream[BigInt] = {
    n #:: numsForm(n + 1)
  }

  def minus(n1: Int, n2: Int): Int = {
    n1 - n2
  }
}
