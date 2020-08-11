package com.hl.match_

/**
 * 描述: match
 * 作者: panhongtong
 * 创建时间: 2020-08-11 11:36
 **/
object MatchDemo {
  def main(args: Array[String]): Unit = {
    for (i <- 0 to 10) {
      i match {
        case _ if i > 5 => println("匹配到了大于5的规则")
        case _ if i == 2 =>
          println("i等于2")
        case 4 => println("i等于4")
        case 7 => println("应该不会被执行了吧")
        case _ =>
          println("默认实现")
          println("打印一下这个值"+_)
      }
    }
  }
}
