package com.hl.match_

/**
 * 描述: 对象匹配
 * 作者: panhongtong
 * 创建时间: 2020-08-11 14:07
 **/
object MatchObject {
  def main(args: Array[String]): Unit = {
    // 对象提取器，主要目的是获得构建参数时所使用的参数
    val num = Square(6.0)

    num match {
      case Square(n) => println(n)
      case _ => println("nothing")
    }
  }
}

object Square {

  // 开方
  def unapply(z:Double): Option[Double] = Some(math.sqrt(z))

  def apply(z: Double): Double = z * z
}
