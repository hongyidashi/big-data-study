package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: join 不推荐使用 相同key的value连在一起，没有相同的key则会丢失
 * cogroup：先进行组内
 * 作者: panhongtong
 * 创建时间: 2020-08-19 09:40
 **/
object RDDOperator9 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))

    val rdd1: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 1), ("D", 2), ("B", 3), ("C", 3)
    ))
    val rdd2: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 4), ("D", 5), ("B", 6), ("C", 7),("F",6)
    ))

    // join (B,(3,6)),(A,(1,4)),(C,(3,7)),(D,(2,5))
    // 如果有重复的key会多次连接
    val joinRDD: RDD[(String, (Int, Int))] = rdd1.join(rdd2)
    println(joinRDD.collect().mkString(","))

    // rightOuterJoin (B,(Some(3),6)),(A,(Some(1),4)),(C,(Some(3),7)),(F,(None,6)),(D,(Some(2),5))
    println(rdd1.rightOuterJoin(rdd2).collect().mkString(","))

    // leftOuterJoin (B,(3,Some(6))),(A,(1,Some(4))),(C,(3,Some(7))),(D,(2,Some(5)))
    println(rdd1.leftOuterJoin(rdd2).collect().mkString(","))

    val rdd3: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 1), ("A", 2), ("B", 3)
    ))
    val rdd4: RDD[(String, Int)] = sc.makeRDD(List(
      ("A", 4), ("D", 5), ("B", 6), ("C", 7),("B",6)
    ))
    // cogroup 先在RDD内进行聚合
    println(rdd3.cogroup(rdd4).collect().mkString(","))

    sc.stop()
  }
}
