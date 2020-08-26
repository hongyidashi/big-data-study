package com.hl.spark.sql.hive

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 描述: 初始化数据
 * 作者: panhongtong
 * 创建时间: 2020-08-25 15:54
 **/
object SparkSQLReq1 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().enableHiveSupport().config(sparkConf)
    .config("spark.sql.warehouse.dir", "hdfs://hadoop103:9000/user/hive/warehouse").getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    spark.sql("create database sparksql")

    spark.sql("use sparksql")

    spark.sql(
      """
        |CREATE TABLE `user_visit_action`(
        |  `date` string,
        |  `user_id` bigint,
        |  `session_id` string,
        |  `page_id` bigint,
        |  `action_time` string,
        |  `search_keyword` string,
        |  `click_category_id` bigint,
        |  `click_product_id` bigint,
        |  `order_category_ids` string,
        |  `order_product_ids` string,
        |  `pay_category_ids` string,
        |  `pay_product_ids` string,
        |  `city_id` bigint)
        |row format delimited fields terminated by '\t'
        |""".stripMargin)

    spark.sql(
      """
        |load data local inpath 'spark-demo/input/req/user_visit_action.txt' into table sparksql.user_visit_action
        |""".stripMargin)

    spark.sql(
      """
        |CREATE TABLE `product_info`(
        |  `product_id` bigint,
        |  `product_name` string,
        |  `extend_info` string)
        |row format delimited fields terminated by '\t'
        |""".stripMargin)

    spark.sql(
      """
        |load data local inpath 'spark-demo/input/req/product_info.txt' into table sparksql.product_info
        |""".stripMargin)

    spark.sql(
      """
        |CREATE TABLE `city_info`(
        |  `city_id` bigint,
        |  `city_name` string,
        |  `area` string)
        |row format delimited fields terminated by '\t'
        |""".stripMargin)

    spark.sql(
      """
        |load data local inpath 'spark-demo/input/req/city_info.txt' into table sparksql.city_info
        |""".stripMargin)

    spark.sql("show databases").show()

    spark.sql(
      """
        |select * from city_info
        |""".stripMargin).show(10)

    // 释放对象
    spark.stop
  }
}
