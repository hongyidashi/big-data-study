package com.hl.spark.core.rdd.serial

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * 描述: 序列化
 * 作者: panhongtong
 * 创建时间: 2020-08-19 14:09
 **/
object SerialDemo {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("RDD-ACTION"))

    val rdd: RDD[Int] = sc.makeRDD(List(4, 2, 3, 1, 5))
    //val rdd: RDD[Int] = sc.makeRDD(List())

    // SparkException: Task not serializable
    // 因为RDD的算子是在分布式节点Executor执行的，所以里面用到的对象需要传输，进而必须实现序列化
    // 即使集合为空，根本不会执行下面的方法也同样会报错，因为spark的算子是闭包的，闭包有可能会包含外部变量
    // 如果包含外部变量，那么一定保证外部变量要可序列化
    // Spark在提交job的时候会检测闭包，判断变量是否可以序列化，如果不能则直接报错，不会提交job
    val user = new MyTestUser
    rdd.foreach(num => println("age="+(user.age + num)))

    sc.stop()
  }

  class MyTestUser extends Serializable {
    var age = 20
  }

}
