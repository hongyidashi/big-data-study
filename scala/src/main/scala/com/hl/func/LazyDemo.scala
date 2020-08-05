package com.hl.func

/**
 * 描述: 懒加载
 * 作者: panhongtong
 * 创建时间: 2020-08-05 14:42
 **/
object LazyDemo {
  def main(args: Array[String]): Unit = {
    lazy val res = sum(10,20)
    println("我应该后执行吧")
    println(res)
  }

  def sum(n1: Int, n2: Int) = {
    println("我应该先执行吧")
    n1 + n1
  }
}
