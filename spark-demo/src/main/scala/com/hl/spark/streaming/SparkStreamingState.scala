package com.hl.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: updateStateByKey
 * 作者: panhongtong
 * 创建时间: 2020-08-28 12:35
 **/
object SparkStreamingState {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))
    // 需要设置检查点，保存中间数据
    ssc.sparkContext.setCheckpointDir("cp")

    val ds = ssc.socketTextStream("localhost", 44444)

    // 有状态的目的是为了将每一个采集周期数据的计算结果保存起来
    // 然后在下一次数据处理中可以使用
    // 需要设置检查点
    ds.flatMap(_.split(" ")).map((_,1L))
        // seq：key和value的集合
        // buffer：相同key的缓冲区的数据，可能为空
        .updateStateByKey[Long]((seq: Seq[Long], buffer: Option[Long]) => {
          val newValue = buffer.getOrElse(0L) + seq.sum
          Option(newValue)
        })
        .print()

    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
