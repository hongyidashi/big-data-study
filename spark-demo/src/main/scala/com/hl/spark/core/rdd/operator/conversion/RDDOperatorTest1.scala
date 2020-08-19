package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 转换算子小练习：获取URL
 * 作者: panhongtong
 * 创建时间: 2020-08-18 10:04
 **/
object RDDOperatorTest1 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("operator"))

    val fileRDD: RDD[String] = sc.textFile("spark-demo/input/apache.log")

    val urlRDD: RDD[String] = fileRDD.map(line => {
      val datas: Array[String] = line.split(" ")
      datas(6)
    })

    urlRDD.collect().foreach(println)
  }
}
