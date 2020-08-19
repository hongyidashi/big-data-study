package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: flatMap 扁平化映射
 * 作者: panhongtong
 * 创建时间: 2020-08-18 10:51
 **/
object RDDOperator2 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("operator"))

    val list = List(List(1, 2), 3, List(4, 5))
    val rdd: RDD[Any] = sc.makeRDD(list)

    val value: RDD[Any] = rdd.flatMap {
      case list: List[_] => list
      case other => List(other)
      case _ => Nil.iterator
    }
    println(value.collect().mkString(","))

    val list2 = List(1, 2, 5, 8, 0, 3, 5, 7, 2, 4)
    val rdd2: RDD[Int] = sc.makeRDD(list2, 3)
    val i: Int = rdd2.glom().map(array => array.max).reduce(_ + _)
    println(i)

    sc.stop()
  }
}
