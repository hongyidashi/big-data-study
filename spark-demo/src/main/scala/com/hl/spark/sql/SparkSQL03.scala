package com.hl.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, LongType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * 描述: SparkSQL 自定义函数 UDAF 弱类型
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL03 {
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
    df.createOrReplaceTempView("dog")

    // 创建函数，注册到环境
    val ageUDAF = new MyAvgAgeUDAF
    spark.udf.register("avgAge", ageUDAF)

    spark.sql("select avgAge(age) from dog").show()

    // 释放对象
    spark.stop()
  }

  /**
   * 自定义聚合函数-弱类型
   */
  class MyAvgAgeUDAF extends UserDefinedAggregateFunction {

    /**
     * 输入数据的结构信息：年龄信息
     */
    override def inputSchema: StructType = {
      StructType(Array(StructField("age", LongType)))
    }

    /**
     * 缓冲区的数据结构信息：年龄总和，人的数量
     */
    override def bufferSchema: StructType = {
      StructType(Array(
        StructField("totalAge", LongType),
        StructField("count", LongType)
      ))
    }

    /**
     * 聚合函数返回值
     */
    override def dataType: DataType = LongType

    /**
     * 函数稳定性
     */
    override def deterministic: Boolean = true

    /**
     * 函数缓冲区的初始化
     */
    override def initialize(buffer: MutableAggregationBuffer): Unit = {
      buffer(0) = 0L
      buffer(1) = 0L
    }

    /**
     * 更新缓冲区
     *
     * @param buffer 缓冲区数据
     * @param input  输入数据
     */
    override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
      buffer(0) = buffer.getLong(0) + input.getLong(0)
      buffer(1) = buffer.getLong(1) + 1
    }

    /**
     * 合并缓冲区
     *
     * @param buffer1
     * @param buffer2
     */
    override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
      buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
      buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)

    }

    /**
     * 计算
     */
    override def evaluate(buffer: Row): Any = {
      buffer.getLong(0) / buffer.getLong(1)
    }
  }

}
