package com.hl.spark.core.rdd.acc

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 广播变量 分布式共享只读变量
 * 作者: panhongtong
 * 创建时间: 2020-08-20 17:31
 **/
object BCDemo {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("RDD-ACTION"))

    val rdd = sc.makeRDD(List( ("a",1), ("b", 2), ("c", 3), ("d", 4) ),4)
    val list = List( ("a",4), ("b", 5), ("c", 6), ("d", 7) )

    // 广播变量
    val bcList: Broadcast[List[(String, Int)]] = sc.broadcast(list)

    val rdd2: RDD[(String, (Int, Int))] = rdd.map {
      case (word, count1) => {
        var count2 = 0
        for ((k, v) <- bcList.value) {
          if (k == word) {
            count2 = v
          }
        }
        (word, (count1, count2))
      }
    }

    println(rdd2.collect().mkString(","))

    sc.stop()
  }
}
