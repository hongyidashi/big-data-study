package com.hl.spark.sql

import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.SparkConf
import org.apache.spark.sql.expressions.Aggregator

/**
 * 描述: SparkSQL 自定义函数 UDAF
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL04 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    val rdd = spark.sparkContext.makeRDD(List(
      ("大福", 20L),
      ("断腿少女", 10L),
      ("狗东西", 30L)
    ))

    val df = rdd.toDF("name", "age")
    val ds = df.as[Dog]

    // 创建自定义函数对象
    val udafClazz = new MyAvgAgeUDAFClazz

    /*
    因为聚合函数是强类型，而SQL中没有强类型这个概念，所以无法使用原来的方式
    可以采用DLS语法进行访问
    将聚合函数转换为DataSet进行访问
     */
    // 将聚合函数转换为查询的列
    ds.select(udafClazz.toColumn).show

    // 释放对象
    spark.stop()
  }

  /**
   * 自定义聚合函数-强类型
   * [输入缓冲区的类型，缓冲区类型，输出类型]
   */
  class MyAvgAgeUDAFClazz extends Aggregator[Dog, AvgBuffer, Long] {

    /**
     * 缓冲区的初始值
     */
    override def zero: AvgBuffer = AvgBuffer(0L, 0L)

    /**
     * 聚合数据
     *
     * @param b
     * @param a
     * @return
     */
    override def reduce(b: AvgBuffer, a: Dog): AvgBuffer = {
      b.sum += a.age
      b.count += 1L
      b
    }

    /**
     * 合并缓冲区
     */
    override def merge(b1: AvgBuffer, b2: AvgBuffer): AvgBuffer = {
      b1.sum += b2.sum
      b1.count += b2.count
      b1
    }

    /**
     * 计算函数结果
     */
    override def finish(reduction: AvgBuffer): Long = reduction.sum / reduction.count

    /**
     * 这两个一般固定写法
     */
    override def bufferEncoder: Encoder[AvgBuffer] = Encoders.product

    override def outputEncoder: Encoder[Long] = Encoders.scalaLong
  }

  /**
   * 计算平均值过程中使用的Buffer
   */
  case class AvgBuffer(var sum: Long, var count: Long)

  case class Dog(name: String, age: Long)

}
