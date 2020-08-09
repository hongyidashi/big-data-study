package com.hl.collection.tuple

/**
 * 描述: 元组
 * 作者: panhongtong
 * 创建时间: 2020-08-09 09:12
 **/
object TupleDemo {
  def main(args: Array[String]): Unit = {
    val tuple01 = ("元素1",0,5,0.444)
    println(tuple01)

    val tuple02 = ()

    println(tuple01._1)
    println(tuple01.productElement(0))

    for (elem <- tuple01.productIterator) {
      println(elem)
    }
  }
}
