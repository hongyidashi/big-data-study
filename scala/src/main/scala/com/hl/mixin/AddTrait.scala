package com.hl.mixin

/**
 * 描述: 叠加特质
 * 作者: panhongtong
 * 创建时间: 2020-08-07 10:56
 **/
object AddTrait {
  def main(args: Array[String]): Unit = {
    val mysql = new Mysql4 with DB4 with File4

    println("----------------------")

    mysql.insert(10)
  }
}

trait Operate4 {
  println("Operate4")
  def insert(id:Int)
}

trait Data4 extends Operate4 {
  println("Data4")

  override def insert(id: Int): Unit = {
    println("插入数据" + id)
  }
}

trait DB4 extends Data4 {
  println("DB4")

  override def insert(id: Int): Unit = {
    println("向数据库")
    super.insert(id)
  }
}

trait File4 extends Data4 {
  println("File4")

  override def insert(id: Int): Unit = {
    println("向文件")
    //super[Data4].insert(id)
    super.insert(id)
  }
}

class Mysql4 {

}
