package com.hl.spark.core

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: WordCount Demo
 * 作者: panhongtong
 * 创建时间: 2020-08-16 22:48
 **/
object WordCount {
  def main(args: Array[String]): Unit = {
    // 准备spark环境
    val sparkConf = new SparkConf().setMaster("local").setAppName("wordCount")

    // 建立和spark连接
    val sc = new SparkContext(sparkConf)

    // 实现业务操作
    // 1. 读取指定目录下的多个文件
    // RDD[T] 是 spark 的数据模型
    val fileRDD: RDD[String] = sc.textFile("spark-demo/input")

    // 2. 将读取的内容扁平化操作，切分单词
    //fileRDD.flatMap(line => line.split(" "))
    val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))

    // 3. 将分词后的数据进行分组
    val groupRDD: RDD[(String, Iterable[String])] = wordRDD.groupBy(word => word)

    // 4. 将分组后的数据进行聚合 （word，count）
    val mapRDD: RDD[(String, Int)] = groupRDD.map { case (word, iter) => (word, iter.size) }

    // 5. 将聚合后的结果采集后打印到控制台
    val wordCountArray: Array[(String, Int)] = mapRDD.collect()
    println(wordCountArray.mkString(","))

    // 释放连接
    sc.stop()
  }
}
