package com.hl.object_clazz

import scala.beans.BeanProperty

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

    // 类也可以加lazy
    lazy val cat1 = new Cat("GDX",3)
    println(cat1)
    println(cat1.inAge)
    println(cat1.getAge)
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

class Cat(inName: String,val inAge: Int) {

  @BeanProperty
  var name = inName
  @BeanProperty
  var age = inAge

  println(inName)

  override def toString = s"Cat($name)"
}