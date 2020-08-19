package com.hl.spark.core.rdd.operator.action

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * 描述: 行动算子
 * 作者: panhongtong
 * 创建时间: 2020-08-19 10:59
 **/
object RDDOperator10 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("RDD-ACTION"))
    val rdd: RDD[Int] = sc.makeRDD(List(4, 2, 3, 1, 5))

    // 简化，规约
    val i: Int = rdd.reduce(_ + _)
    println(i)

    // 采集数据
    // 会将所有数据采集到内存中，如果数据量大则可能会出现内存溢出的问题
    val array: Array[Int] = rdd.collect()
    println(array)

    // 计数
    val countL: Long = rdd.count()
    println(countL)
    println("------------------------")

    // 排序后取3个
    val ints: Array[Int] = rdd.takeOrdered(3)
    ints.foreach(println)
    println("------------------------")

    val rdd2: RDD[Int] = sc.makeRDD(List(1,2,3,4))
    // 这里的foreach是方法，方法是在当前节点执行的
    rdd2.collect().foreach(println)
    println("*************************")
    // 这里的foreach是算子，算子是在分布式节点Executor执行的
    rdd2.foreach(println)

  }
}
