package com.hl.match_

/**
 * 描述: match匹配
 * 作者: panhongtong
 * 创建时间: 2020-08-11 12:04
 **/
object MatchDemo2 {
  def main(args: Array[String]): Unit = {
    // 匹配数组
    val arrs = Array(Array(0),Array(),Array(0,3),Array(0,1,2,3,4,5))
    for (arr <- arrs) {
      val res = arr match {
        case Array(0) => "只有一个元素且为0的数组"
        case Array(x,y) => "有两个元素的数组"
        case Array(0,_*) => "以0开头的数组"
        case _ => "不在匹配范围内"
      }
      println(res)
    }
    println("=======================")

    //匹配集合
    val lists = Array(List(0),List(),List(0,3),List(0,1,2,3,4,5))
    for (list <- lists) {
      val res = list match {
        case 0 :: Nil => "只有0的集合"
        case x :: y :: Nil => "有两个元素的集合"
        case 0 :: tail => "0 ..."
        case _ => "其他处理"
      }
      println(res)
    }
    println("=======================")


    //匹配元组
    val tuples = Array((7,0),(),(0,3),(0,1,2,3,4,5))
    for (tuple <- tuples) {
      val res = tuple match {
        case (0,_) => "0 ..."
        case (x,y) => "有两个元素的集合"
        case _ => "其他处理"
      }
      println(res)
    }
  }
}
