package com.hl.spark.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: Kafka数据源
 * 作者: panhongtong
 * 创建时间: 2020-08-26 15:07
 **/
object SparkStreamingKafka {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    // 定义Kafka参数
    val kafkaPara: Map[String, Object] = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop103:9092,hadoop104:9092,hadoop105:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "hl",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    // 读取Kafka数据创建DStream
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Set("hl"), kafkaPara)
    )

    // 将每条消息的KV取出
    val valueDStream: DStream[String] = kafkaDStream.map(record => record.value())

    // 计算WordCount
    valueDStream.flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
      .print()


    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }
}
