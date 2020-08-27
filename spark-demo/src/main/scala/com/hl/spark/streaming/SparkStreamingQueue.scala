package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

/**
 * 描述: 采集队列
 * 作者: panhongtong
 * 创建时间: 2020-08-26 15:07
 **/
object SparkStreamingQueue {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    val queue = new mutable.Queue[RDD[String]]()
    val queueDS: InputDStream[String] = ssc.queueStream(queue)

    queueDS.print()

    // 启动采集器
    ssc.start()

    for (i <- 1 to 8) {
      val rdd = ssc.sparkContext.makeRDD(List(i.toString))
      queue.enqueue(rdd)
      Thread.sleep(1000)
    }

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
