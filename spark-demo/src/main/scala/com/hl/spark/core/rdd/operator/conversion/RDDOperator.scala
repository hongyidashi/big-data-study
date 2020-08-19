package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: Operator：map、mapPartitions、mapPartitionsWithIndex
 * 作者: panhongtong
 * 创建时间: 2020-08-17 15:41
 **/
object RDDOperator {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("operator"))

    // map
    val list = List(1, 2, 3, 4, 5)
    val rdd: RDD[Int] = sc.makeRDD(list)
    val rdd2: RDD[Int] = rdd.map(_ * 2)
    println(rdd2.collect().mkString(","))

    // mapPartitions,可以用于数据过滤
    val list2 = List(1, 6, 3, 4, 5, 8, 5, 10)
    val rdd3: RDD[Int] = sc.makeRDD(list2,2)
    // 求分区最大值
    val maxRDD: RDD[Int] = rdd3.mapPartitions(iter => {
      // 拿到的是一个迭代器，要求返回的是也一个迭代器
      List(iter.max).iterator
    })
    println(maxRDD.collect().mkString(","))

    // mapPartitionsWithIndex，带分区号
    val partAndMaxVal: RDD[(Int, Int)] = rdd3.mapPartitionsWithIndex((partNum, iter) => {
      List((partNum, iter.max)).iterator
    })
    println(partAndMaxVal.collect().mkString(","))
    // 关闭连接
    sc.stop()
  }
}
