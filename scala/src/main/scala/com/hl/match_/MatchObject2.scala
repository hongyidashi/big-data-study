package com.hl.match_

/**
 * 描述: 对象提取
 * 作者: panhongtong
 * 创建时间: 2020-08-11 14:57
 **/
object MatchObject2 {
  def main(args: Array[String]): Unit = {
    //val names = "GDX,JK,UZI"
    val names = MyNames("大福", "智障", "大傻子")
    names match {
      case MyNames(n1, n2, n3) => println(s"$n1 + $n2 + $n3")
      case _ => println("啥都没有")
    }
  }
}

object MyNames {
  def apply(n1: String, n2: String, n3: String): String = n1 + "," + n2 + "," + n3

  def unapplySeq(str: String): Option[Seq[String]] = {
    if (str.contains(",")) Some(str.split(","))
    else None
  }
}
