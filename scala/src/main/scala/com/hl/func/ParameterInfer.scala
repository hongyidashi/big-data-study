package com.hl.func

/**
 * 描述: 类型推断
 * 作者: panhongtong
 * 创建时间: 2020-08-12 17:03
 **/
object ParameterInfer {
  def main(args: Array[String]): Unit = {
    val list = List(1, 2, 3, 4)

    println(list.map((x:Int) => x + 1))
    println(list.map(x => x + 1))
    println(list.map(_ + 1))

    println(list.reduce((n1: Int, n2: Int) => n1 + n2))
    println(list.reduce(_ + _))

  }
}
