package com.hl.collection.queue

import scala.collection.mutable._

/**
 * 描述: 队列
 * 作者: panhongtong
 * 创建时间: 2020-08-10 10:08
 **/
object QueueDemo {
  def main(args: Array[String]): Unit = {
    val q1 = new Queue[Any]
    q1 += 1
    println(q1)

    q1 ++= List(2, 3, 4)
    println(q1)

    q1 += List(5, 6, 7)
    println(q1)

    q1.dequeue()
    println(q1)

    q1.enqueue(8)
    println(q1)

    println(q1.head)
    println(q1.last)
    println(q1)

    println(q1.tail)
    println(q1.tail.tail)
  }
}
