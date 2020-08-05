package com.hl.objcet

/**
 * 描述: 类示例
 * 作者: panhongtong
 * 创建时间: 2020-08-05 16:22
 **/
object ObjectDemo {
  def main(args: Array[String]): Unit = {
    val dog = new Dog
    dog.name = "大福"
    dog.age = 3
    println(dog)

    val dog2 = new Dog
    println(dog2)

    val dog3 = new Dog("弟弟", 5)
    println(dog3)
  }
}

class Dog {

  var name: String = _
  var age: Int = _

  def this(inName: String, inAge: Int) {
    this
    name = inName
    age = inAge
  }

  override def toString = s"Dog($name, $age)"

}
