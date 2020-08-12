package com.hl.func

/**
 * 描述: 高阶函数和匿名函数
 * 作者: panhongtong
 * 创建时间: 2020-08-12 16:21
 **/
object HighOrderFunDemo2 {
  def main(args: Array[String]): Unit = {
    // 高阶函数一
    def test(f1: Int => Int,f2:Int => Double,n:Int) = {
      f2(f1(n))
    }
    val fun = (i:Int) => i.toDouble
    println(test(sum,fun,4))

    // 高阶函数二
    def minusxy(x: Int) = {
      y:Int => x - y
    }

    val hFunc1 = minusxy(3)
    println(hFunc1(1))
    println(hFunc1(6))

    println(minusxy(1)(29))

  }

  def sum(n: Int): Int = {
    n << 1
  }
}
