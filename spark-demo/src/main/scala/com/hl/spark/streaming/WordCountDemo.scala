package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: streaming-wc
 * 作者: panhongtong
 * 创建时间: 2020-08-26 12:07
 **/
object WordCountDemo {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    val socketDS: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 44444)

    val wordDS = socketDS.flatMap(_.split(" "))
    val wordToOneDS = wordDS.map((_, 1))
    val wordToSumDS = wordToOneDS.reduceByKey(_ + _)

    wordToSumDS.print()

    // 启动采集器
    ssc.start()
    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
