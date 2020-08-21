package com.hl.summer.framework.util

import org.apache.spark.{SparkConf, SparkContext}

object EnvUtil {

  private val scLocal = new ThreadLocal[SparkContext]

  def getEnv() = {
    // 从当前==线程==的共享内存空间中获取环境对象
    var sc: SparkContext = scLocal.get()
    if (sc == null){
      // 如果获取不到环境对象，则创建并保存到共享内存中
      val sparkConf = new SparkConf()
        .setMaster("local[4]")
        .setAppName("sparkApplication")
      sc = new SparkContext(sparkConf)
      scLocal.set(sc)
    }
    sc
  }

  def clear():Unit = {
    getEnv().stop()
    // 将共享内存中的数据清除
    scLocal.remove()
  }
}