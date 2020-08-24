package com.hl.spark.core.req.service

import com.hl.spark.core.req.dao.HotCategorySessionAnalysisTop10Dao
import com.hl.summer.framework.core.TService
import org.apache.spark.rdd.RDD

/**
 * 描述: 
 * 作者: panhongtong
 * 创建时间: 2020-08-24 14:14
 **/
class PageFlowService extends TService {

  private val hotCategorySessionAnalysisTop10Dao = new HotCategorySessionAnalysisTop10Dao

  /**
   * 对页部分面进行统计
   */
  override def analysis(): Any = {
    // 获取用户的行为数据
    val actionRDD = hotCategorySessionAnalysisTop10Dao.getUserVisitAction("spark-demo/input/user_visit_action.txt")

    // 缓存，用于分子计算
    actionRDD.cache()

    // 计算分母
    val pageList = List(1, 2, 3, 4, 5)
    val needFlows = pageList.zip(pageList.tail).map(t => t._1 + "-" + t._2)

    // 过滤掉不需要统计的页面
    val pageCountArray = actionRDD.filter(action => {
      // init 去掉尾部元素
      pageList.init.contains(action.page_id.toInt)
    }).map(action => {
      (action.page_id, 1)
    }).reduceByKey(_ + _).collect()


    // 计算分子
    // 根据session_id分组，并根据时间排序
    val pageFlowRDD: RDD[(String, List[(String, Int)])] = actionRDD.groupBy(_.session_id).mapValues(iter => {
      val actions = iter.toList.sortWith((left, right) => {
        left.action_time < right.action_time
      })

      // 对排序后进行转换 => ( (1-2,1),(2-3,1) )
      val pageIds = actions.map(_.page_id)
      pageIds.zip(pageIds.tail).map {
        case (pageId1, pageId2) => (pageId1 + "-" + pageId2, 1)
      }.filter{
        // 过滤掉不需要的跳转数据
        case (ids,_) => needFlows.contains(ids)
      }
    })

    // 将分组后的数据结构进行转换并统计
    // List[(String, Int)] => (String, Int)
    val pageFlowToSumRDD = pageFlowRDD.map(_._2).flatMap(list => list).reduceByKey(_ + _)


    // 计算转换率
    pageFlowToSumRDD.foreach {
      case (pageFlow, sum) => {
        val pageId = pageFlow.split("-")(0)
        val value = pageCountArray.toMap.getOrElse(pageId.toLong, 1)
        println("页面" + pageId + "的转换率为：" + sum.toDouble / value)
      }
    }
  }

  /**
   * 对所有页面进行统计
   */
  def analysis2(): Any = {
    // 获取用户的行为数据
    val actionRDD = hotCategorySessionAnalysisTop10Dao.getUserVisitAction("spark-demo/input/user_visit_action.txt")

    // 缓存，用于分子计算
    actionRDD.cache()

    // 计算分母
    val pageCountArray = actionRDD.map(action => {
      (action.page_id, 1)
    }).reduceByKey(_ + _).collect()


    // 计算分子
    // 根据session_id分组，并根据时间排序
    val pageFlowRDD: RDD[(String, List[(String, Int)])] = actionRDD.groupBy(_.session_id).mapValues(iter => {
      val actions = iter.toList.sortWith((left, right) => {
        left.action_time < right.action_time
      })

      // 对排序后进行转换 => ( (1-2,1),(2-3,1) )
      val pageIds = actions.map(_.page_id)
      pageIds.zip(pageIds.tail).map {
        case (pageId1, pageId2) => (pageId1 + "-" + pageId2, 1)
      }
    })

    // 将分组后的数据结构进行转换并统计
    // List[(String, Int)] => (String, Int)
    val pageFlowToSumRDD = pageFlowRDD.map(_._2).flatMap(list => list).reduceByKey(_ + _)


    // 计算转换率
    pageFlowToSumRDD.foreach {
      case (pageFlow, sum) => {
        val pageId = pageFlow.split("-")(0)
        val value = pageCountArray.toMap.getOrElse(pageId.toLong, 1)
        println("页面" + pageId + "的转换率为：" + sum.toDouble / value)
      }
    }
  }
}
