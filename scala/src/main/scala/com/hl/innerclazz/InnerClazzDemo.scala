package com.hl.innerclazz


/**
 * 描述: 内部类
 * 作者: panhongtong
 * 创建时间: 2020-08-07 14:10
 **/
object InnerClazzDemo {
  def main(args: Array[String]): Unit = {
    val outer1 = new ScalaOuterClazz

    // 成员内部类的使用
    val inner1 = new outer1.ScalaInnerClazz
    inner1.sayHi

    // 静态内部类的使用
    val inner2 = new ScalaOuterClazz.ScalaStaticInnerClazz
    inner2.sayHello

    inner1.sayFuck

    // 类型投影
    val outer2 = new ScalaOuterClazz
    val inner3 = new outer2.ScalaInnerClazz
    inner1.sayWhat(inner3)
  }
}

class ScalaOuterClazz {

  myouter =>
  class ScalaInnerClazz {
    def sayHi {
      println("嗨，成员内部类")
    }

    def sayFuck {
      println(ScalaOuterClazz.this.age + "说了句FUCK")
      println(myouter.age + "岁了今年")
    }

    def sayWhat(ic: ScalaOuterClazz#ScalaInnerClazz): Unit = {
      println("啥玩意？")
    }
  }

  var name = "大福"
  var age = 3
}

object ScalaOuterClazz {
  class ScalaStaticInnerClazz {
    def sayHello: Unit = {
      println("hello，静态内部类")
    }
  }
}
