package com.hl.func

/**
 * 描述: 高阶函数
 * 作者: panhongtong
 * 创建时间: 2020-08-10 12:18
 **/
object HighOrderFunDemo {
  def main(args: Array[String]): Unit = {
    println(highOrderFun(sum,3))

    val func1 = sayHello
    val func2 = sayHello _

    func2()
  }

  def sayHello() = {
    println("hello 莫西莫西")
  }

  def highOrderFun(f: Int => Int,n:Int) = {
    f(n)
  }

  def sum(n: Int): Int = {
    n << 1
  }
}
