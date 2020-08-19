package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 双Value
 * 作者: panhongtong
 * 创建时间: 2020-08-18 14:45
 **/
object RDDOperator5 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))
    val rdd1: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4, 5))
    val rdd2: RDD[Int] = sc.makeRDD(List(2, 3, 4, 5, 6))

    // 交集
    println(rdd1.intersection(rdd2).collect().mkString(","))

    // 并集
    println(rdd1.union(rdd2).collect().mkString(","))

    // 差集
    println(rdd1.subtract(rdd2).collect().mkString(","))

    // 拉链
    println(rdd1.zip(rdd2).collect().mkString(","))

    sc.stop()
  }
}
