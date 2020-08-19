package com.hl.spark.core.rdd.persist

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * 描述: 持久化-缓存
 * 作者: panhongtong
 * 创建时间: 2020-08-19 22:23
 **/
object PersistDemo {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("RDD-ACTION"))

    val rdd: RDD[Int] = sc.makeRDD(List(4, 2, 3, 1, 5))

    val mapRDD: RDD[(Int, Int)] = rdd.map(num => {
      println("map...")
      (num, 1)
    })

    // 将计算结果进行缓存，重复使用
    // cache底层调用的是persist()，persist在持久化数据的时候会采用不同的存储级别对数据进行存储
    // cache操作默认是将数据存到内存中；如果内存不够用，Executor会整理数据，可以丢弃某些数据
    // 如果因为Executor整理内存而丢失数据，那么只能重新执行获得完整的数据
    // 如果要从新执行操作的话就要遵循血缘关系，所以cache操作不能删除血缘关系。
    mapRDD.cache()

    println(mapRDD.collect().mkString(","))

    mapRDD.saveAsTextFile("spark-demo/output")

    sc.stop()
  }
}
