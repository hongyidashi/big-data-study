package com.hl.match_

/**
 * 描述: ForMatch
 * 作者: panhongtong
 * 创建时间: 2020-08-11 15:19
 **/
object ForMatch {
  def main(args: Array[String]): Unit = {
    val map = Map("A" -> 5, "B" -> 4, "C" -> 5, "D" -> 2)

    for ((k,5) <- map) {
      println(k)
    }
  }
}
