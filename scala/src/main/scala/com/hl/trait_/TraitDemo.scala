package com.hl.trait_

/**
 * 描述: trait演示
 * 作者: panhongtong
 * 创建时间: 2020-08-06 22:53
 **/
object TraitDemo {
  def main(args: Array[String]): Unit = {
    var objA = new A
    var objB = new B

    objA.getConnect()
    objB.getConnect()
  }
}

trait trait1 {
  def getConnect()
}

class A extends trait1 {

  override def getConnect(): Unit = {
    println("获取连接")
  }
}

object A {
  def apply: A = new A
}

class B extends A {

}

object B {
  def apply: B = new B
}
