package com.hl.collection.array

/**
 * 描述: Array Demo
 * 作者: panhongtong
 * 创建时间: 2020-08-08 22:50
 **/
object ArrayDemo {
  def main(args: Array[String]): Unit = {
    val arr01 = new Array[Any](5)
    arr01(0) = "大福"
    arr01(1) = 5

    println(arr01.length)

    for (i <- arr01) {
      println(i)
    }

    val arr02 = new Array[Int](2)
    arr02(0) = 2
    for (i <- arr02) {
      println(i)
    }

    println("---------------")

    var arr03 = Array(1, 4, "断腿少女")
    for (i <- 0 until arr03.length) {
      println(arr03(i))
    }
    println(arr03.length)
    println("---------------")

    // 多维数组
    val arrDim = Array.ofDim[Any](3, 4)
    arrDim(0)(0) = "断腿"
    arrDim(0)(1) = "少女"
    arrDim(1)(0) = "智障"
    arrDim(1)(1) = "大傻子"
    for (i <- 0 until arrDim.length) {
      for (j <- 0 until arrDim(i).length) {
        println(s"($i,$j) => " + arrDim(i)(j))
      }
    }

  }
}
