package com.hl.spark.core.wc

import com.hl.summer.framework.core.TApplication
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
 * 描述: 用reduce实现wc
 * 作者: panhongtong
 * 创建时间: 2020-08-21 11:11
 **/
object WordCount4 extends App with TApplication{
  start("spark") {
    val sc: SparkContext = envData.asInstanceOf[SparkContext]

    val rdd: RDD[String] = sc.makeRDD(List("b", "c", "a", "b", "a"))

    val mapRDD: RDD[Map[String, Int]] = rdd.map(word => Map((word, 1)))

    val mapValue1: Map[String, Int] = mapRDD.reduce((map1, map2) => {
      map1.foldLeft(map2)(
        // 这个map就是map2，kv是map1的
        // 柯里化，map2成为了方法的一部分
        (map, kv) => {
          map.updated(kv._1, map.getOrElse(kv._1, 0) + kv._2)
        }
      )
    })
    //println(mapValue1)

    val mapValue2: Map[String, Int] = rdd.aggregate(Map[String, Int]())(
      // 分区内规则
      (map, k) => {
        map.updated(k, map.getOrElse(k, 0) + 1)
      },
      (map1, map2) => {
        map1.foldLeft(map2)(
          // 这个map就是map2，kv是map1的
          // 柯里化，map2成为了方法的一部分
          (map, kv) => {
            map.updated(kv._1, map.getOrElse(kv._1, 0) + kv._2)
          }
        )
      }
    )
    println(mapValue2)

  }
}
