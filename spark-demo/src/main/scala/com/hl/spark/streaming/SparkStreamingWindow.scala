package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: Window 窗口滑动
 * 作者: panhongtong
 * 创建时间: 2020-08-28 12:35
 **/
object SparkStreamingWindow {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))
    // 需要设置检查点，保存中间数据
    ssc.sparkContext.setCheckpointDir("cp")

    val ds = ssc.socketTextStream("localhost", 44444)

    // 将多个采集周期作为整体计算
    // 窗口范围是周期的整数倍
    // 窗口大小默认是一个周期
    ds.flatMap(_.split(" ")).map((_,1L))
        // seq：key和value的集合
        // buffer：相同key的缓冲区的数据，可能为空
        .window(Seconds(9))
        .print()

    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
