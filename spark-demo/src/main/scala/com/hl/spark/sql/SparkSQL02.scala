package com.hl.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 描述: SparkSQL 自定义函数
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL02 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    val rdd = spark.sparkContext.makeRDD(List(
      ("大福", 20),
      ("断腿少女", 10),
      ("狗东西", 30)
    ))

    val df = rdd.toDF("name", "age")
    df.createOrReplaceTempView("dog")

    spark.udf.register("addName", (x: String) => "Name:" + x)

    spark.sql("select addName(name),age from dog").show()

    // 释放对象
    spark.stop()
  }

  case class Dog(name: String, age: Int)

}
