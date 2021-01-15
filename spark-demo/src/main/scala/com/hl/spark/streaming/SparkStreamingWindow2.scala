package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: Window 窗口滑动
 * 作者: panhongtong
 * 创建时间: 2020-08-28 12:35
 **/
object SparkStreamingWindow2 {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))
    // 需要设置检查点，保存中间数据
    ssc.sparkContext.setCheckpointDir("cp")

    val ds = ssc.socketTextStream("localhost", 44444)

    val result = ds.map(num => ("key", num.toInt)).reduceByKeyAndWindow(
      (x, y) => {
        x + y
      },
      (x, y) => {
        x - y
      },
      Seconds(9)
    )

    result.foreachRDD(rdd => rdd.foreach(println))

    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
