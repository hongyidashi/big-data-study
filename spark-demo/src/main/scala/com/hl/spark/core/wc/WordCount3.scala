package com.hl.spark.core.wc

import com.hl.summer.framework.core.TApplication
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
 * 描述: 测试框架环境
 * 作者: panhongtong
 * 创建时间: 2020-08-20 23:10
 **/
object WordCount3 extends App with TApplication {
  start("spark") {
    val sc: SparkContext = envData.asInstanceOf[SparkContext]

    val fileRDD: RDD[String] = sc.textFile("spark-demo/input/wc1.txt")

    val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))

    val mapRDD: RDD[(String, Int)] = wordRDD.map(word => (word, 1))

    val wordToSumRDD: RDD[(String, Int)] = mapRDD.reduceByKey(_ + _)

    val wordCountArray: Array[(String, Int)] = wordToSumRDD.collect()
    println(wordCountArray.mkString(","))
  }
}
