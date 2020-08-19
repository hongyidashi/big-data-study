package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: aggregateByKey
 * 作者: panhongtong
 * 创建时间: 2020-08-18 17:09
 **/
object RDDOperator7 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))
    val rdd: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 1), ("A", 2), ("B", 3),
      ("B", 4), ("B", 5), ("C", 6)
    ), 2)

    // 分区内求最大值，分区间求和
    // 第一个参数 zeroValue：初始值
    // 第二个参数 seqOp：分区内计算规则
    // 第三个参数 combOp：分区间计算规则
    val result: RDD[(String, Int)] = rdd.aggregateByKey(0)(
      (x, y) => math.max(x, y),
      (x, y) => x + y
    )
    println(result.collect().mkString(","))

    // 如果分区内和分区间计算规则相同，可用 foldByKey
    rdd.foldByKey(0)(_+_)

    sc.stop()
  }
}
