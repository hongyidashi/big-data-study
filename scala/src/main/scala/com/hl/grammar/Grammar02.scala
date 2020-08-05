package com.hl.grammar

import util.control.Breaks._

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-08-04 15:56
 **/
object Grammar02 {
  def main(args: Array[String]): Unit = {
    val num1 = 10
    val num2 = 20

    val maxV1 = if (num1 > num2) num1 else num2
    println(maxV1)

    for (i <- 0 to 3) {
      println(i)
    }
    println("---------------------")

    for (i <- 6 to 10 if i != 7) {
      println(i)
    }
    println("---------------------")

    for (i <- 10 to 15; j = i - 2 if j != 12) {
      println(s"i=$i,j=$j")
    }
    println("---------------------")

    val res1 = for (i <- 0 to 5) yield i
    println(res1)
    println("---------------------")

    val res2 = for (i <- 0 to 5) yield {
      "狗东西" + i
    }
    println(res2)
    println("---------------------")

    for (i <- Range(0, 10, 2)) {
      println(i)
    }
    println("---------------------")

    var num3 = 0
    breakable(
      while (num3 < 10) {
        if (num3 == 7) {
          break()
        }
        println(num3)
        num3 += 1
      }
    )
    println("OK,出来了")
    println("---------------------")

    println("res:"+getRes(num1,num2,'+'))

  }

  def getRes(n1: Int, n2: Int, oper: Char) = {
    if (oper == '+') {
      n1 + n2
    } else if (oper == '-') {
      n1 - n2
    } else {
      null
    }
  }
}
