package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: combineByKey 根据key合并，可将计算的第一个值进行结构转换
 * 作者: panhongtong
 * 创建时间: 2020-08-18 21:58
 **/
object RDDOperator8 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))
    val rdd: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 1), ("A", 2), ("B", 3), ("C", 3),
      ("B", 4), ("B", 5), ("C", 6), ("A", 7)
    ), 2)

    val value: RDD[(String, (Int, Int))] = rdd.combineByKey(
      // 第一个参数表示的是将计算的第一个值转换结构
      v => (v, 1),
      // 第二个参数表示的是区内计算规则
      (t: (Int, Int), v) => {
        (t._1 + v, t._2 + 1)
      },
      // 第三个参数表示的是区间计算规则
      (t1: (Int, Int), t2: (Int, Int)) => {
        (t1._1 + t2._1, t1._2 + t2._2)
      }
    )

    println(value.map {
      case (key, (total, count)) => (key, total / count)
    }.collect().mkString(","))

    sc.stop()
  }
}
