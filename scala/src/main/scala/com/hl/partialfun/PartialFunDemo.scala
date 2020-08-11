package com.hl.partialfun

/**
 * 描述: 偏函数
 * 作者: panhongtong
 * 创建时间: 2020-08-11 17:15
 **/
object PartialFunDemo {
  def main(args: Array[String]): Unit = {
    val list = List(1,2,3,4,"hello")

    // 参数：[输入类型，输出类型]
    val partialFunc = new PartialFunction[Any,Int] {
      // 判断如果是true则调用apply，false则不调用
      override def isDefinedAt(x: Any): Boolean = x.isInstanceOf[Int]

      override def apply(v1: Any): Int = {
        v1.asInstanceOf[Int] + 1
      }
    }

    val resList = list.collect(partialFunc)
    println(resList)
  }
}
