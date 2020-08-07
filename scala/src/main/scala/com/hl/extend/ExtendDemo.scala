package com.hl.extend

/**
 * 描述: 继承
 * 作者: panhongtong
 * 创建时间: 2020-08-06 14:48
 **/
object ExtendDemo {
  def main(args: Array[String]): Unit = {
    var p1 = new Person
    var s1 = new Student
    p1 = s1
    p1.asInstanceOf[Student].sayHello

    val emp:Emp = new Employee
    println(emp.age)
    emp.work
    emp.call
  }
}

abstract class Emp {
  var name: String
  val age: Int = 10
  var no: Int = 1
  def work(): String
  def call() = {
    println("hollow")
  }
}

class Employee extends Emp {
  override var name: String = _
  override val age = 20
  //override var no = 20
  override def work(): String = {
    println("爷在工作")
    "工作"
  }
}

class Person {
  var name: String = _

  def sayHi: Unit = {
    println("hi")
  }

}

class Student extends Person {
  def sayHello: Unit = {
    println("hello")
  }
}
