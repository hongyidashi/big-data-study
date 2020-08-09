package com.hl.collection.list

import scala.collection.mutable.ListBuffer

/**
 * 描述: ListBuffer
 * 作者: panhongtong
 * 创建时间: 2020-08-09 09:51
 **/
object ListBufferDemo {
  def main(args: Array[String]): Unit = {
    val list = ListBuffer("大福","断腿少女",1,7,1,1,1,1,1,1)
    println(list)
    list.addOne("沙雕")
    println(list)
    list += "真的可以？"
    println(list)

    list -= 1
    println(list)
    list -= list(1)
    println(list)
    list.remove(1,3)
    println(list)

    val res = list.result()
    println(res)
  }
}
