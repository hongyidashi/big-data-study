package com.hl.spark.core.req.service

import java.io

import com.hl.spark.core.req.bean.HotCategory
import com.hl.spark.core.req.dao.HotCategoryAnalysisTop10Dao
import com.hl.summer.framework.core.TService
import org.apache.spark.rdd.RDD

/**
 * 描述: 业务层
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:46
 **/
class HotCategoryAnalysisTop10Service extends TService {

  private val hotCategoryAnalysisTop10Dao = new HotCategoryAnalysisTop10Dao

  /**
   *
   * @return
   */
  override def analysis() = {
    // 1. 读取数据
    val fileRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("spark-demo/input/user_visit_action.txt")

    val flatMapRDD = fileRDD.flatMap(line => {
      val data = line.split("_")
      if (data(6) != "-1") {
        List((data(6), HotCategory(data(6), 1, 0, 0)))
      } else if (data(8) != "null") {
        val ids: Array[String] = data(8).split(",")
        ids.map(id => (id, HotCategory(id, 0, 1, 0)))
      } else if (data(10) != "null") {
        val ids: Array[String] = data(10).split(",")
        ids.map(id => (id, HotCategory(id, 0, 0, 1)))
      } else {
        Nil
      }
    })

    val reduceRDD: RDD[(String, HotCategory)] = flatMapRDD.reduceByKey((c1, c2) => {
      c1.clickCount += c2.clickCount
      c1.orderCount += c2.orderCount
      c1.payCount += c2.payCount
      c1
    })
    val result = reduceRDD.collect().sortWith((left, right) => {
      val leftHC = left._2
      val rightHC = right._2
      if (leftHC.clickCount > rightHC.clickCount |
        leftHC.orderCount > rightHC.orderCount |
        leftHC.payCount > rightHC.payCount) {
        true
      } else {
        false
      }
    }).take(10)
    result

  }

  /**
   * 优化版本，使用flatMap构建数据模型，减少map
   * @return
   */
  def analysis3() = {
    // 1. 读取数据
    val fileRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("spark-demo/input/user_visit_action.txt")

    val flatMapRDD: RDD[(String, (Int, Int, Int))] = fileRDD.flatMap(line => {
      val data = line.split("_")
      if (data(6) != "-1") {
        List((data(6), (1, 0, 0)))
      } else if (data(8) != "null") {
        val ids: Array[String] = data(8).split(",")
        ids.map(id => (id, (0, 1, 0)))
      } else if (data(10) != "null") {
        val ids: Array[String] = data(10).split(",")
        ids.map(id => (id, (0, 0, 1)))
      } else {
        Nil
      }
    })

    val reduceRDD: RDD[(String, (Int, Int, Int))] = flatMapRDD.reduceByKey(
      (t1, t2) => {
        (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
      }
    )
    reduceRDD.sortBy(_._2, false).take(10)

  }

  /**
   * 基础版
   * @return
   */
  def analysis2(): Array[(String, (Int, Int, Int))] = {
    // 1. 读取数据
    val fileRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("spark-demo/input/user_visit_action.txt")

    fileRDD.cache()

    // 2. 统计点击，过滤无用数据
    val clickRDD: RDD[(String, Int)] = fileRDD.map(line => {
      val data: Array[String] = line.split("_")
      (data(6), 1)
    }).filter(_._1 != "-1")
    val categoryIdToClickCountRDD: RDD[(String, Int)] = clickRDD.reduceByKey(_ + _)

    // 偏函数方式
    //    def statisticsClick:PartialFunction[RDD[String],RDD[(String, Int)]] = {
    //      case fRDD: RDD[String] => fileRDD.map(line => {
    //        val datas: Array[String] = line.split("_")
    //        (datas(6), 1)
    //      }).filter(_._1 != "-1").reduceByKey(_+_)
    //    }

    // 3. 下单统计，过滤无用数据
    val orderRDD: RDD[String] = fileRDD.map(line => {
      val data: Array[String] = line.split("_")
      data(8)
    }).filter(_ != "null")
    val categoryIdToOrderRDD: RDD[(String, Int)] = orderRDD.flatMap(id => {
      val ids: Array[String] = id.split(",")
      ids.map(id => (id, 1))
    }).reduceByKey(_ + _)

    // 4. 对支付进行统计，过滤无用数据
    val payRDD: RDD[String] = fileRDD.map(line => {
      val data: Array[String] = line.split("_")
      data(10)
    }).filter(_ != "null")
    val categoryIdToPayRDD: RDD[(String, Int)] = payRDD.flatMap(id => {
      val ids: Array[String] = id.split(",")
      ids.map(id => (id, 1))
    }).reduceByKey(_ + _)

    // 5. 将结果进行结构转换
    //    val joinRDD: RDD[(String, ((Int, Int), Int))] = categoryIdToClickCountRDD.join(categoryIdToOrderRDD).join(categoryIdToPayRDD)
    //    // 希望key保持不变只对value操作可以用 mapValues
    //    val mapRDD: RDD[(String, (Int, Int, Int))] = joinRDD.mapValues {
    //      case ((clikCount, orderCount), payCount) => (clikCount, orderCount, payCount)
    //    }
    // 优化后
    val newCategoryIdToClickCountRDD: RDD[(String, (Int, Int, Int))] = categoryIdToClickCountRDD.map {
      case (id, count) => (id, (count, 0, 0))
    }
    val newCategoryIdToOrderRDD: RDD[(String, (Int, Int, Int))] = categoryIdToOrderRDD.map {
      case (id, count) => (id, (0, count, 0))
    }
    val newCategoryIdToClickPayRDD: RDD[(String, (Int, Int, Int))] = categoryIdToPayRDD.map {
      case (id, count) => (id, (0, 0, count))
    }

    val countRDD: RDD[(String, (Int, Int, Int))] = newCategoryIdToClickCountRDD.union(newCategoryIdToOrderRDD).union(newCategoryIdToClickPayRDD)
    val mapRDD: RDD[(String, (Int, Int, Int))] = countRDD.reduceByKey(
      (t1, t2) => {
        (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
      }
    )

    // 6. 排序，value是元组tuple，tuple天生支持排序，默认升序，改为降序
    val sortRDD: RDD[(String, (Int, Int, Int))] = mapRDD.sortBy(_._2, false)

    // 7. 取前10，返回
    val result: Array[(String, (Int, Int, Int))] = sortRDD.take(10)
    result
  }
}
