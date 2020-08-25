package com.hl.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}

/**
 * 描述: SparkSQL demo
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL01 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    // SQL
    // 将DF转化为临时视图
    /*
    中间遇到的问题：
    Since Spark 2.3, the queries from raw JSON/CSV files are disallowed when the
    referenced columns only include the internal corrupt record column
    => Spark2.3后，json不允许多行，要单行即可
     */
    val jsonDF = spark.read.json("spark-demo/input/user.json")
    jsonDF.createOrReplaceTempView("user")
    spark.sql("select * from user").show()

    // DSL
    // 如果采用单引号操作，那么需要引入 import spark.implicits._
    jsonDF.select('name, 'age).show

    val rdd = spark.sparkContext.makeRDD(List(
      ("大福", 20),
      ("断腿少女", 10),
      ("狗东西", 30)
    ))
    // RDD <=> DataFrame
    val df = rdd.toDF("name", "age")
    val dfToRDD = df.rdd

    // RDD <=> DataSet
    val dogRDD: RDD[Dog] = rdd.map {
      case (name, age) => Dog(name, age)
    }
    val dogDS: Dataset[Dog] = dogRDD.toDS()
    val dsToRDD = dogDS.rdd

    // DataFrame <=> DataSet
    val dfToDS = df.as[Dog]
    val dsToDF = dfToDS.toDF()

    // 释放对象
    spark.stop()
  }

  case class Dog(name: String, age: Int)

}
