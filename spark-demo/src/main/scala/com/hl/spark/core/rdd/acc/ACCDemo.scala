package com.hl.spark.core.rdd.acc

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 描述: 累加器<br>
 *   Accumulator：分布式共享只写变量
 *   1. 将累加器变量注册到spark中
 *   2. 执行计算时，spark会将累加变量放到executor中执行计算
 *   3. 计算完毕后，executor会将累加器结果返回到driver端
 *   4. driver端获取到多个累加器的结果，两两合并，最后的得到运行结果
 * 作者: panhongtong
 * 创建时间: 2020-08-20 14:04
 **/
object ACCDemo {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("RDD-ACTION"))

    val rdd: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4))

    //var sum = 0
    val sum = sc.longAccumulator("sum")

    rdd.foreach(count => {
      //sum = count + 1
      sum.add(count)
    })

    // 0 因为闭包的原因
    //println(sum)
    println(sum.value)

    sc.stop()
  }
}
