package com.hl.spark.core.rdd.operator

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * 描述: 获取第二个分区的数据
 * 作者: panhongtong
 * 创建时间: 2020-08-18 10:29
 **/
object RDDOperatorTest2 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("operator"))

    val list2 = List(1, 6, 3, 4, 5, 8, 5, 10, 6, 0, 3)

    val rdd: RDD[Int] = sc.makeRDD(list2, 3)
    val value: RDD[Int] = rdd.mapPartitionsWithIndex((index, iter) => {
      if (index == 2) {
        iter
      } else {
        Nil.iterator
      }
    })

    println(value.collect().mkString(","))
    sc.stop()
  }
}
