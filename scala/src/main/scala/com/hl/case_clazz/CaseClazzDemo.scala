package com.hl.case_clazz

/**
 * 描述: 样例类
 * 作者: panhongtong
 * 创建时间: 2020-08-11 15:36
 **/
object CaseClazzDemo {
  def main(args: Array[String]): Unit = {
    for (amt <- Array(Dollar(1000.0),Currency(10,"RMB"),NoAmout)) {
      val res = amt match {
        case Dollar(v) => "￥"+v
        case Currency(v,u) => v + " " + u
        case NoAmout => "空"
      }
      println(res)
    }
  }
}

abstract class Amount
case class Dollar(value:Double) extends Amount
case class Currency(value:Double,unit:String) extends Amount
case class NoAmout() extends Amount
