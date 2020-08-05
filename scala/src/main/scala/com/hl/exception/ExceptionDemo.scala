package com.hl.exception

/**
 * 描述: Scala异常处理
 * 作者: panhongtong
 * 创建时间: 2020-08-05 15:35
 **/
object ExceptionDemo {
  def main(args: Array[String]): Unit = {
    try {
      val i = 10 / 0
      println(i)
      throw new Exception
    } catch {
      case _: ArithmeticException => println("算数异常")
      case ex: Exception => {
        println("Exception被捕获")
        ex.printStackTrace()
      }
    } finally {
      println("finally被执行")
    }
  }
}
