package com.hl.spark.core.req.helper

import com.hl.spark.core.req.bean.HotCategory
import org.apache.spark.util.AccumulatorV2

import scala.collection.mutable

/**
 * 描述: 累加器<br>
 * 输入输出：<br>
 * [(品类id,行为类别),mutable.Map[品类id,HotCategory]]
 * 作者: panhongtong
 * 创建时间: 2020-08-23 22:46
 **/
class HotCategoryAccumulator extends AccumulatorV2[(String, String), mutable.Map[String, HotCategory]] {

  /**
   * 返回类型
   */
  val hotCategoryMap = mutable.Map[String, HotCategory]()

  override def isZero: Boolean = hotCategoryMap.isEmpty

  override def copy(): AccumulatorV2[(String, String), mutable.Map[String, HotCategory]] = new HotCategoryAccumulator

  override def reset(): Unit = hotCategoryMap.clear()

  override def add(v: (String, String)): Unit = {
    val cid = v._1
    val actionType = v._2

    val hotCategory = hotCategoryMap.getOrElse(cid, HotCategory(cid, 0, 0, 0))

    actionType match {
      case "click" => hotCategory.clickCount += 1
      case "order" => hotCategory.orderCount += 1
      case "pay" => hotCategory.payCount += 1
      case _ =>
    }

    hotCategoryMap(cid) = hotCategory
  }

  override def merge(other: AccumulatorV2[(String, String), mutable.Map[String, HotCategory]]): Unit = {
    other.value.foreach{
      case (cid,hotCategory) => {
        // 将 hotCategory 与 hc 进行累加
        val hc = hotCategoryMap.getOrElse(cid, HotCategory(cid, 0, 0, 0))
        hc.clickCount += hotCategory.clickCount
        hc.orderCount += hotCategory.orderCount
        hc.payCount += hotCategory.payCount

        hotCategoryMap(cid) = hc
      }
    }
  }

  override def value: mutable.Map[String, HotCategory] = hotCategoryMap
}
