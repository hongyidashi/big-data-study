package com.hl.mixin

/**
 * 描述: 特质中重写抽象方法
 * 作者: panhongtong
 * 创建时间: 2020-08-07 11:53
 **/
object MinInDemo {
  def main(args: Array[String]): Unit = {
    val mysql5 = new Mysql5 with DB5 with File5
    mysql5.insert(100)

  }
}

trait Operate5 {
  def insert(id:Int)
}

trait File5 extends Operate5 {
  abstract override def insert(id:Int): Unit = {
    println("File5的insert被执行")
    super.insert(id)
  }
}

trait DB5 extends Operate5 {
  override def insert(id: Int): Unit = {
    println("DB5的insert被执行")
  }
}

class Mysql5 {
}

