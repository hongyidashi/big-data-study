package com.hl.spark.core.rdd.operator.conversion

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, Partitioner, SparkConf, SparkContext}

/**
 * 描述: partitionBy-根据指定规则对数据进行分区<br>
 * partitionBy 是针对 K-V 形式的拓展功能，RDD里没有这个方法，是RDD伴生对象进行了隐式转换使其具有了该功能
 * 作者: panhongtong
 * 创建时间: 2020-08-18 15:27
 **/
object RDDOperator6 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("wordCount"))

    val rdd: RDD[(String, Int)] = sc.makeRDD(List(("nba", 1), ("cba", 2), ("cba", 3), ("aba", 4)))

    // HashPartitioner：对对象的hashcode进行取余
    val hashRDD: RDD[(String, Int)] = rdd.partitionBy(new HashPartitioner(3))
    println(hashRDD.glom().collect().length)

    // 自定义分区器
    val myRDD: RDD[(String, Int)] = rdd.partitionBy(new MyPartitioner(2)).mapPartitionsWithIndex((index, datas) => {
      if (index == 0) datas else Nil.iterator
    })
    println(myRDD.collect().mkString(","))

    sc.stop()
  }

  /**
   * 自定义分区器
   *
   * @param num 分区数
   */
  class MyPartitioner(num: Int) extends Partitioner {

    /**
     * 分区数
     */
    override def numPartitions: Int = {
      num
    }

    /**
     * 根据key返回分区号
     */
    override def getPartition(key: Any): Int = {
      key match {
        case "cba" => 0
        case _ => 1
      }
    }
  }

}
