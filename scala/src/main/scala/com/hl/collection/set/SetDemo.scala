package com.hl.collection.set

import scala.collection.mutable

/**
 * 描述: set集
 * 作者: panhongtong
 * 创建时间: 2020-08-10 11:19
 **/
object SetDemo {
  def main(args: Array[String]): Unit = {
    val set = mutable.Set(3,4,1,4,6,9)
    println(set)

    println(set.max)

    set += 10
    println(set)
  }
}
