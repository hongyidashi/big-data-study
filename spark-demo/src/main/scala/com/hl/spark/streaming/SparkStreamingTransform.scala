package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: Transform
 * 作者: panhongtong
 * 创建时间: 2020-08-28 12:35
 **/
object SparkStreamingTransform {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    val ds = ssc.socketTextStream("localhost", 44444)

    // 通过 transform 可以获取到RDD
    ds.transform(
      rdd => {
        rdd.map(_*2)
      }
    )

    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
