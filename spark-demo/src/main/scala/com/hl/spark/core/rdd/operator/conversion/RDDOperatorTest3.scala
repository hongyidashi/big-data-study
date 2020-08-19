package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 从服务器日志数据apache.log中获取2015年5月17日的请求路径
 * 作者: panhongtong
 * 创建时间: 2020-08-18 11:45
 **/
object RDDOperatorTest3 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("operator"))

    val fileRDD: RDD[String] = sc.textFile("spark-demo/input/apache.log")

    val urlRDD: RDD[(String, String)] = fileRDD.filter(line => {
      val datas: Array[String] = line.split(" ")
      datas(3).startsWith("17/05/2015")
    }).map(line => {
      val datas: Array[String] = line.split(" ")
      (datas(3), datas(6))
    })

    urlRDD.collect().foreach(println)
  }
}
