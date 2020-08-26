package com.hl.spark.sql.hive

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, LongType, MapType, StringType, StructField, StructType}

/**
 * 描述: 计算各个区域前三大热门商品，并备注上每个商品在主要城市中的分布比例，
 * 超过两个城市用其他显示
 * 作者: panhongtong
 * 创建时间: 2020-08-25 15:54
 **/
object SparkSQLReq3 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    import spark.implicits._

    // 注册自定义函数
    val cityRemarkUDAF = new CityRemarkUDAF
    spark.udf.register("cityUDAF", cityRemarkUDAF)

    spark.sql("use sparksql")

    spark.sql(
      """
        |select
        |	a.*,
        |	c.area,
        |	p.product_name,
        | c.city_name
        |from
        |	user_visit_action a
        |	join city_info c on c.city_id = a.city_id
        |	join product_info p on p.product_id = a.click_product_id
        |where a.click_product_id > -1
        |""".stripMargin).createOrReplaceTempView("t1")

    spark.sql(
      """
        |select
        |	area,
        |	product_name,
        |	count(*) as clickCount,
        | cityUDAF(city_name)
        |from t1
        |group by area,product_name
        |""".stripMargin).createOrReplaceTempView("t2")

    spark.sql(
      """
        |select
        |	*,
        |	rank() over (partition by area order by clickCount desc) as rank
        |from t2
        |""".stripMargin).createOrReplaceTempView("t3")

    spark.sql(
      """
        |select * from t3
        |where rank <= 3
        |""".stripMargin).show()

    // 释放对象
    spark.stop
  }

  /**
   * 自定义城市备注聚合函数
   */
  class CityRemarkUDAF extends UserDefinedAggregateFunction {
    override def inputSchema: StructType = {
      StructType(Array(StructField("cityName", StringType)))
    }

    override def bufferSchema: StructType = {
      StructType(Array(
        StructField("totalcnt", LongType),
        StructField("cityMap", MapType(StringType, LongType))
      ))
    }

    override def dataType: DataType = StringType

    override def deterministic: Boolean = true

    override def initialize(buffer: MutableAggregationBuffer): Unit = {
      buffer(0) = 0L
      buffer(1) = Map[String, Long]()
    }

    override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
      val cityName = input.getString(0)

      // 点击次数+1
      buffer(0) = buffer.getLong(0) + 1
      // 城市点击+1
      val cityMap = buffer.getAs[Map[String, Long]](1)
      val newClickCount = cityMap.getOrElse(cityName, 0L) + 1
      buffer(1) = cityMap.updated(cityName, newClickCount)
    }

    override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
      buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)

      val cityMap1 = buffer1.getAs[Map[String, Long]](1)
      val cityMap2 = buffer2.getAs[Map[String, Long]](1)

      buffer1(1) = cityMap1.foldLeft(cityMap2) {
        case (map, (k, v)) => {
          map.updated(k, map.getOrElse(k, 0L) + v)
        }
      }
    }

    override def evaluate(buffer: Row): Any = {
      val totalCount = buffer.getLong(0)
      val cityMap = buffer.getMap[String, Long](1)

      val cityToCountList = cityMap.toList.sortWith((left, right) => {
        left._2 > right._2
      }).take(2)

      val hasRest = cityMap.size > 2
      var rest = 0L
      val str = new StringBuilder

      cityToCountList.foreach{
        case (city,count) => {
          val r = count * 100 / totalCount
          str.append(city + ":" + r + "% ")
          rest += r
        }
      }

      if (hasRest) {
        str.append("其他:"+(100-rest)+ "%").toString()
      } else {
        str.toString()
      }

    }
  }

}
