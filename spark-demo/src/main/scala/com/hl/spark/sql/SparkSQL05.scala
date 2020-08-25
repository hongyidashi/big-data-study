package com.hl.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 描述: SparkSQL 通用读取和保存<br>
 *   通用指的是使用相同的API，根据不同的参数读取和保存不同格式的数据
 * 作者: panhongtong
 * 创建时间: 2020-08-25 09:41
 **/
object SparkSQL05 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    //import spark.implicits._

    /*
    RuntimeException: file:xxx/input/user.json is not a Parquet file.
    错误原因：不是一个列式(Parquet)存储文件
    SparkSQL通用读取默认的数据格式为Parquet列式存储格式
     */
    //val frame = spark.read.load("spark-demo/input/user.json")
    val frame = spark.read.load("spark-demo/input/users.parquet")
    frame.show()

    // 读取json格式
    val frameJson = spark.read.format("json").load("spark-demo/input/user.json")
    frameJson.show()

    // 直接在文件上进行查询
    spark.sql("select * from json.`spark-demo/input/user.json`").show()


    /*
    写的流程操作跟读差不多
    保存模式：
      case "overwrite" => SaveMode.Overwrite      如果文件已经存在则覆盖
      case "append" => SaveMode.Append            如果文件已经存在则追加
      case "ignore" => SaveMode.Ignore            如果文件已经存在则忽略
      case "error" | "errorifexists" | "default"  如果文件已经存在则抛出异常
     */
    frame.write.mode("overwrite").format("json").save("spark-demo/output")

    // 释放对象
    spark.stop()
  }

}
