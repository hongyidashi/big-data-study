package com.hl.implicit_

/**
 * 描述: 隐式转换
 * 作者: panhongtong
 * 创建时间: 2020-08-07 14:30
 **/
object ImplicitDemo {
  def main(args: Array[String]): Unit = {

    implicit def DtoI(double: Double): Int = {
      double.toInt
    }

    val num1: Int = 3.5
    println(num1)

    implicit def CtoD(cat: Cat): Dog = {
      val dog = new Dog
      dog.dogName = cat.catName
      dog.age = cat.age
      dog
    }

    val cat = new Cat("大福", 6)
    val dog: Dog = cat
    println(dog)

    implicit def DtoC(dog: Dog): Cat = {
      new Cat("断腿少女",3)
    }

    dog.dogCall
    dog.miao

    println("----------------")

    def hello(implicit name:String = "啥玩意") {
      println(name + "hello")
    }

    implicit val num = 10
    hello

    implicit val str1 = "弟弟"
    //implicit val str2 = "GDX"  //打开会报错
    hello

  }
}

class Cat(inName: String, inAge: Int) {
  var catName: String = inName
  var age: Int = inAge

  def miao: Unit = {
    println("瞄~")
  }
}

class Dog {
  var dogName: String = _
  var age: Int = _

  def dogCall: Unit = {
    println("汪！")
  }

  override def toString = s"Dog($dogName, $age)"
}
