package com.hl.spark.core.req.service

import com.hl.spark.core.req.bean.HotCategory
import com.hl.spark.core.req.dao.HotCategorySessionAnalysisTop10Dao
import com.hl.summer.framework.core.TService
import com.hl.summer.framework.util.EnvUtil

/**
 * 描述: 业务层
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:46
 **/
class HotCategorySessionAnalysisTop10Service extends TService {

  private val hotCategorySessionAnalysisTop10Dao = new HotCategorySessionAnalysisTop10Dao

  /**
   * 数据分析
   *
   * @return
   */
  override def analysis(data: Any) = {
    val hotCategoriesTop10 = data.asInstanceOf[List[HotCategory]]

    val top10Ids = hotCategoriesTop10.map(_.categoryId)
    // 使用广播变量传输 top10Ids
    val bcList = EnvUtil.getEnv().broadcast(top10Ids)

    // 获取用户的行为数据
    val actionRDD = hotCategorySessionAnalysisTop10Dao.getUserVisitAction("spark-demo/input/user_visit_action.txt")

    // 过滤：
    // 1. 必须是点击事件； 2. 类别必须在top10内
    val filterRDD = actionRDD.filter(
      acction => {
        if (acction.click_category_id != -1) {
          bcList.value.contains(acction.click_category_id.toString)
//          var flag = false
//          hotCategoriesTop10.foreach(hc => {
//            if (hc.categoryId.toLong == acction.click_category_id) {
//              flag = true
//            }
//          })
//          flag
        } else {
          false
        }
      }
    )

    // 对结果进行转换和统计计算
    // 输入数据 => (品类id_sessionId,1) => 根据key统计 => (品类id,sessionId,count)
    val reduceRDD = filterRDD.map(acction => {
      (acction.click_category_id + "_" + acction.session_id, 1)
    }).reduceByKey(_ + _).map {
      case (key, count) => {
        val ks = key.split("_")
        (ks(0), (ks(1), count))
      }
    }

    // 分组排序，取得前十
    val resultRDD = reduceRDD.groupByKey().mapValues(iter => {
      iter.toList.sortWith((left, right) => {
        left._2 > right._2
      }).take(10)
    })

    // 得到结果
    resultRDD.collect()
  }

}
