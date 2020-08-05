package com.hl.func

/**
 * 描述: 递归
 *
 * 作者: panhongtong
 * 创建时间: 2020-08-05 10:01
 **/
object Recursion {
  def main(args: Array[String]): Unit = {
    println(fibonacci(7))
    println(peach(8))
  }

  def peach(day: Int): Int = {
    if (day == 10) {
      1
    } else {
      (peach(day+1) + 1) * 2
    }
  }

  def fibonacci(n: Int): Int = {
    if (n == 1 || n == 2) {
      1
    } else {
      fibonacci(n - 1) + fibonacci(n - 2)
    }
  }

}
