package com.hl.grammar

/**
 * 描述: 练习——乘法口诀表
 * 作者: panhongtong
 * 创建时间: 2020-08-05 15:51
 **/
object Exercise {
  def main(args: Array[String]): Unit = {
    table
  }

  // 乘法口诀表
  def table() {
    for (i <- 1 to 9) {
      for (j <- 1 to i) {
        printf(s"$i × $j = ${i * j}  ")
      }
      println()
    }
  }
}
