package com.hl.spark.core.rdd.acc

import org.apache.spark.rdd.RDD
import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
 * 描述: 自定义累加器-WordCount
 * 作者: panhongtong
 * 创建时间: 2020-08-20 15:47
 **/
object ACCDemo2 {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("RDD-ACTION"))

    val rdd: RDD[String] = sc.makeRDD(List("hello", "spark", "scala", "hello", "hello world"))

    // 1. 创建一个累加器
    val accumulator = new MyWordCountAccumulator
    // 2. 注册累加器
    sc.register(accumulator)
    // 3. 使用累加器
    rdd.flatMap(_.split(" ")).foreach {
      word => {
        accumulator.add(word)
      }
    }

    // 4. 获取值
    println(accumulator.value)

    sc.stop()
  }

  /**
   * 自定义 wordcount累加器<br>
   * AccumulatorV2[IN, OUT]：[输入类型,输出类型]
   */
  class MyWordCountAccumulator extends AccumulatorV2[String, mutable.Map[String, Int]] {

    // 存储word出现次数的集合
    var wordCountMap = mutable.Map[String, Int]()

    /**
     * 是否初始化
     */
    override def isZero: Boolean = wordCountMap.isEmpty

    /**
     * 复制累加器
     */
    override def copy(): AccumulatorV2[String, mutable.Map[String, Int]] = new MyWordCountAccumulator

    /**
     * 重置累加器
     */
    override def reset(): Unit = wordCountMap.clear()

    /**
     * 向累加器中添加值
     */
    override def add(v: String): Unit = {
      wordCountMap(v) = wordCountMap.getOrElse(v, 0) + 1
    }

    /**
     * 合并其他累加器
     */
    override def merge(other: AccumulatorV2[String, mutable.Map[String, Int]]): Unit = {
      val map1 = wordCountMap
      val map2 = other.value
      // 这部分是折叠
      // 相当于把map1所有元素都折叠到了map2中，再把map赋值到wordCountMap中
      wordCountMap = map1.foldLeft(map2)((map, kv) => {
        map(kv._1) = map.getOrElse(kv._1, 0) + kv._2
        map
      })
    }

    /**
     * 返回累加结果
     */
    override def value: mutable.Map[String, Int] = wordCountMap
  }

}
