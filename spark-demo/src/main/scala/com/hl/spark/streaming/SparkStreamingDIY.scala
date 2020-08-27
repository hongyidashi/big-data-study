package com.hl.spark.streaming

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 描述: DIY采集器
 * 作者: panhongtong
 * 创建时间: 2020-08-26 15:07
 **/
object SparkStreamingDIY {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SpartStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(3))

    // 使用自定义采集器
    val ds: ReceiverInputDStream[String] = ssc.receiverStream(new MyReceiver("localhost", 44444))
    ds.print()


    // 启动采集器
    ssc.start()

    // 等待采集器的结束
    ssc.awaitTermination()
  }

  /**
   * 自定义采集器
   * 需指定存储级别，这里选择仅在内存中存储
   * 泛型是指定数据类型
   */
  class MyReceiver(host: String, port: Int) extends Receiver[String](StorageLevel.MEMORY_ONLY) {

    private var socket: Socket = _

    def receive(): Unit = {
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"))

      var content: String = null

      while (true) {
        content = reader.readLine()
        if (content != null) {
          // 将获取的数据保存到框架内部进行封装
          store(content)
        }
      }
    }

    override def onStart(): Unit = {
      socket = new Socket(host, port)

      new Thread("Socket Receiver") {
        setDaemon(true)

        override def run() {
          receive()
        }
      }.start()
    }

    override def onStop(): Unit = {
      if (socket != null) {
        socket.close()
        socket = null
      }
    }
  }

}
