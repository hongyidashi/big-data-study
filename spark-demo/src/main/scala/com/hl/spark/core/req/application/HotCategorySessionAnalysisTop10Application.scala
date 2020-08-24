package com.hl.spark.core.req.application

import com.hl.spark.core.req.controller.HotCategorySessionAnalysisTop10Controller
import com.hl.summer.framework.core.TApplication

/**
 * 描述: 需求二：Top10热门品类中每个品类的Top10活跃Session统计<br>
 *   在需求一的基础上，增加每个品类用户session的点击统计
 * 作者: panhongtong
 * 创建时间: 2020-08-24
 **/
object HotCategorySessionAnalysisTop10Application extends App with TApplication{

  start("spark"){
    val controller = new HotCategorySessionAnalysisTop10Controller
    controller.execute()
  }
}
