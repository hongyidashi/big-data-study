package com.hl.spark.core.req.controller

import com.hl.spark.core.req.service.{HotCategoryAnalysisTop10Service, HotCategorySessionAnalysisTop10Service}
import com.hl.summer.framework.core.TController

/**
 * 描述: 控制层
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:46
 **/
class HotCategorySessionAnalysisTop10Controller extends TController{

  private val hotCategoryAnalysisTop10Service = new HotCategoryAnalysisTop10Service
  private val hotCategorySessionAnalysisTop10Service = new HotCategorySessionAnalysisTop10Service

  override def execute() {
    val categories = hotCategoryAnalysisTop10Service.analysis()
    val result = hotCategorySessionAnalysisTop10Service.analysis(categories)
    result.foreach(println)
  }
}
