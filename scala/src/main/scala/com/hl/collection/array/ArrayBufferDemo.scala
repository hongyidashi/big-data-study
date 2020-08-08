package com.hl.collection.array

import scala.collection.mutable.ArrayBuffer

/**
 * 描述: 变长数组
 * 作者: panhongtong
 * 创建时间: 2020-08-08 23:04
 **/
object ArrayBufferDemo {
  def main(args: Array[String]): Unit = {
    val arr01 = ArrayBuffer("大福","我的天啊")
    arr01.foreach(elem => println(elem))
    println(arr01.size)

    println("-----------------------")

    val arr02 = new ArrayBuffer[Any]()
    println(arr02.hashCode())
    arr02.append("太难了")
    println(arr02.hashCode())
    arr02.append(10)
    println(arr02.hashCode())
    arr02(1) = 20
    println(arr02.hashCode())
    arr02.remove(1)
    println(arr02.hashCode())

    val arr03 = arr02.toArray
  }
}
