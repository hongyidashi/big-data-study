package com.hl.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 描述: SparkSQL 通用读取和保存<br>
 *   加载、写入MySQL数据
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL06 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    // 加载MySQL数据
    val frame = spark.read.format("jdbc")
      .option("url", "jdbc:mysql://172.16.10.21/my_big_data")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "123456")
      .option("dbtable", "dog")
      .load()

    frame.show()

    // 写入MySQL
    frame.write.format("jdbc")
      .option("url", "jdbc:mysql://172.16.10.21/my_big_data")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "123456")
      .option("dbtable", "dog1")
      .save()

    // 释放对象
    spark.stop
  }

}
