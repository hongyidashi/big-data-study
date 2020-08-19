package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: coalesce-缩减分区数量 repartition-重分区
 * repartition 底层调用的是 coalesce，但 coalesce 不能增加分区，因为 coalesce
 * 默认情况下是没有经过shuffle（打乱数据重新组合），所以即使分区数大于原分区数也无效
 * repartition 底层：
 * coalesce(numPartitions, shuffle = true)
 * 作者: panhongtong
 * 创建时间: 2020-08-18 14:11
 **/
object RDDOperator4 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))
    val rdd: RDD[Int] = sc.makeRDD(List(1, 1, 1, 2, 2, 2), 2)
    val value: RDD[Int] = rdd.filter(num => num % 2 == 0)

    println(value.glom().collect().length)
    // 缩减分区数量
    println(value.coalesce(1).glom().collect().length)

    // 重分区，可增加分区数量
    println(value.repartition(3).glom().collect().length)
    sc.stop()
  }
}
