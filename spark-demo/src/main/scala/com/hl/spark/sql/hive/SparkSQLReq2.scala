package com.hl.spark.sql.hive

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 描述: 计算各个区域前三大热门商品，并备注上每个商品在主要城市中的分布比例，
 * 超过两个城市用其他显示
 * 作者: panhongtong
 * 创建时间: 2020-08-25 15:54
 **/
object SparkSQLReq2 {
  def main(args: Array[String]): Unit = {
    // 构建环境对象
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQL")
    val spark = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()

    // 隐式转换导入，这里的spark指的是环境对象，即上面那个我们自己构建的spark
    // 要求这个对象必须使用val
    //import spark.implicits._

    spark.sql("use sparksql")

    spark.sql(
      """
        |select * from(
        |	select
        |		*,
        |		rank() over (partition by area order by clickCount desc) as rank
        |	from(
        |		select
        |			area,
        |			product_name,
        |			count(*) as clickCount
        |		from(
        |				select
        |					a.*,
        |					c.area,
        |					p.product_name
        |				from
        |					user_visit_action a
        |					join city_info c on a.city_id = c.city_id
        |					join product_info p on a.click_product_id = p.product_id
        |				where a.click_product_id > -1
        |			) t1
        |		group by area,product_name
        |	) t2
        |) t3
        |where rank <= 3
        |""".stripMargin).show()

    // 释放对象
    spark.stop
  }
}
