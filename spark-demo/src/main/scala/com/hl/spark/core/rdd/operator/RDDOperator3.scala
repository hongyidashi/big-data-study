package com.hl.spark.core.rdd.operator

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: groupBy-分组
 * 作者: panhongtong
 * 创建时间: 2020-08-18 11:39
 **/
object RDDOperator3 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))

    val list = List(1, 2, 3, 4, 5, 6)
    val rdd: RDD[Int] = sc.makeRDD(list, 3)

    // groupBy 根据key分组
    val value: RDD[(Int, Iterable[Int])] = rdd.groupBy(_ % 2, 2)

    println(value.collect().mkString(","))
    println(value.glom().collect().length)

    // filter 过滤偶数
    val value2: RDD[Int] = rdd.filter(_ % 2 == 0)
    println(value2.collect().mkString(","))
    sc.stop()
  }
}
