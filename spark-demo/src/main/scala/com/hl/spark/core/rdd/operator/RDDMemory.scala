package com.hl.spark.core.rdd.operator

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 从内存中读取数据创建RDD
 * 作者: panhongtong
 * 创建时间: 2020-08-17 15:41
 **/
object RDDMemory {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))

    // 从内存中读取数据创建RDD
    val list = List(1, 2, 3, 4, 5)
    val rdd: RDD[Int] = sc.parallelize(list)
    rdd.collect().foreach(println)
    rdd.saveAsTextFile("spark-demo/output")

    // 关闭连接
    sc.stop()
  }
}
