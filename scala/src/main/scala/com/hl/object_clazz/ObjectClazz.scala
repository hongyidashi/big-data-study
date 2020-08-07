package com.hl.object_clazz

/**
 * 描述: 伴生类和伴生对象
 * 作者: panhongtong
 * 创建时间: 2020-08-06 17:27
 **/
object ObjectClazz {
  def main(args: Array[String]): Unit = {
    val child1 = new Child("大福")
    val child2 = new Child("断腿少女")
    val child3 = new Child("GDX")

    Child.joinGame(child1)
    Child.joinGame(child2)
    Child.joinGame(child3)

    Child.total
  }
}

class Child(inName: String) {
  var name: String = inName
}

object Child {

  var totalNum: Int = _

  def joinGame(child: Child) = {
    println(child.name + "加入了游戏")
    totalNum += 1
  }

  def total = {
    println(totalNum)
  }
}