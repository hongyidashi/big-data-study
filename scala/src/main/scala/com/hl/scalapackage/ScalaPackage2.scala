package com.hl.scalapackage

/**
 * 描述: 导包测试
 * 作者: panhongtong
 * 创建时间: 2020-08-06 11:45
 **/
object ScalaPackage2 {
  def main(args: Array[String]): Unit = {
    var obj = new MyScalaPackageClazz
    println(obj.name)

    import java.util.{HashMap => JavaHashMap}
    var map = new JavaHashMap
    import scala.collection.mutable._
    var map2 = new HashMap
  }
}

class MyScalaPackageClazz {
  private[scalapackage] var name = "大福"
}
