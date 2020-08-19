package com.hl.spark.core.rdd.persist

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 持久化-检查点
 * 作者: panhongtong
 * 创建时间: 2020-08-19 22:23
 **/
object PersistDemo2 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("RDD-ACTION"))

    sc.setCheckpointDir("spark-demo/cp")

    val rdd: RDD[Int] = sc.makeRDD(List(1,2,3))

    val mapRDD: RDD[(Int, Int)] = rdd.map(num => {
      println("map...")
      (num, 1)
    })

    // 一般会把比较耗时、重要的数据不会存在内存中，而是保存在分布式文件系统中
    // checkpoint：将数据保存到检查点，使用前应添加保存路径 sc.setCheckpointDir(dir)
    // 为了保证数据的准确性，执行时，会启动新的job，也就是会多执行一次（执行2次）
    // 为了提高性能，一般会将checkpoint和cache联合使用
    // cache先对数据进行缓存，然后checkpoint将缓存中的数据持久化到文件
    // checkpoint会切断依赖关系，数据丢失不会重新执行获取
    // 因为将数据存储在分布式文件系统中，我们认为是相对安全的，不容易丢失
    // 等同于产生了新的数据源
    mapRDD.cache()
    mapRDD.checkpoint()

    println(mapRDD.collect().mkString(","))
    println(mapRDD.collect().mkString("-"))
    println(mapRDD.collect().mkString("&"))


    sc.stop()
  }
}
