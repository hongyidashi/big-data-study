package com.hl.spark.core.req.application

import com.hl.spark.core.req.controller.HotCategoryAnalysisTop10Controller
import com.hl.summer.framework.core.TApplication

/**
 * 描述: 需求一：Top10热门品类<br>
 *   先按照点击数排名，靠前的就排名高；如果点击数相同，再比较下单数；下单数再相同，就比较支付数
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:43
 **/
object HotCategoryAnalysisTop10Application extends App with TApplication{

  start("spark"){
    val controller = new HotCategoryAnalysisTop10Controller
    controller.execute()
  }
}
