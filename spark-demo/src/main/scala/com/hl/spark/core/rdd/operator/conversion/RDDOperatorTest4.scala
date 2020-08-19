package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 统计出每一个省份每个广告被点击数量排行的Top3
 * 作者: panhongtong
 * 创建时间: 2020-08-19 10:12
 **/
object RDDOperatorTest4 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))

    // 1. 获取原始数据
    val fileRDD: RDD[String] = sc.textFile("spark-demo/input/agent.log")

    // 2. 将原始数据进行结构转换，方便统计 => ((省份,广告),1)
    val mapRDD: RDD[(String, Int)] = fileRDD.map(line => {
      val data: Array[String] = line.split(" ")
      (data(1) + "-" + data(4), 1)
    })

    // 3. 将相同key进行聚合 => ((省份,广告),sum)
    val reduceRDD: RDD[(String, Int)] = mapRDD.reduceByKey(_ + _)

    // 4. 将聚合后的结果进行转换 => (省份,(广告,sum))
    val mapRDD2: RDD[(String, (String, Int))] = reduceRDD.map {
      case (key, sum) => {
        val keys: Array[String] = key.split("-")
        (keys(0), (keys(1), sum))
      }
    }

    // 5. 将相同省份是数据放在同一个组中 => (省份,Iterator[(广告1,sum1),(广告2,sum2)])
    val groupRDD: RDD[(String, Iterable[(String, Int)])] = mapRDD2.groupByKey()

    // 6. 将分组后的数据进行排序（降序），取TOP3
    // mapValues：值处理value不处理key
    val sortRDD: RDD[(String, List[(String, Int)])] = groupRDD.mapValues(iter => {
      iter.toList.sortWith((left, right) => {
        left._2 > right._2
      }).take(3)
    })

    // 7. 将结果打印在控制台上
    sortRDD.collect().foreach(println)

    sc.stop()
  }
}
