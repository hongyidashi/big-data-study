package com.hl.grammar

object Grammar01 {
  def main(args: Array[String]): Unit = {
    var num1: Int = 10
    var num2 = num1.toDouble
    println(num1.toDouble)
    println(num2 + 1)
    println(1.2.toInt)
    println("hello".substring(0, 1))

    var num3 = 2
    var num4 = 6

    num3 *= num4
    num4 = num3/num4
    num3 = num3/num4
    print(s"num3=$num3,num4=$num4")
    // 语法
  }
}
