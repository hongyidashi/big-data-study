package com.hl.case_clazz

/**
 * 描述: 打折案例
 * 作者: panhongtong
 * 创建时间: 2020-08-11 16:02
 **/
object SalesDemo {
  def main(args: Array[String]): Unit = {
    val sale = Bundle("书籍", 10, Book("漫画", 40), Bundle("文学作品", 20, Book("活着", 80), Book("围城", 30)))

    sale match {
      case Bundle(_,_,Book(desc,_),_*) => println(desc)
    }

    sale match {
      case Bundle(_,_,art @ Book(_,_),rest @ _*) => println(art,rest)
    }

    // 求价格
    def price(item: Item): Double = {
      item match {
        case Book(_, price) => price
        case Bundle(_, discount, item@_*) => item.map(price).sum - discount
        case _ => -9999999
      }
    }
    println(price(sale))
  }
}

abstract class Item

case class Book(description: String, price: Double) extends Item

case class Bundle(description: String, discount: Double, item: Item*) extends Item