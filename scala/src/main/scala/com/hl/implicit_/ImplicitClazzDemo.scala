package com.hl.implicit_

/**
 * 描述: 隐式类
 * 作者: panhongtong
 * 创建时间: 2020-08-07 16:50
 **/
object ImplicitClazzDemo {
  def main(args: Array[String]): Unit = {
    implicit class ImplicitClazz(val dog: Dog) {
      def dogDogCall: Unit = {
        println("汪汪汪~")
      }
    }

    val dog = new Dog
    dog.dogDogCall

    val dog2 = new Dog
    dog2.dogDogCall
  }
}