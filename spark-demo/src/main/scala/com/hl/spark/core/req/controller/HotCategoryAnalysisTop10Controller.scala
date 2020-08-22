package com.hl.spark.core.req.controller

import com.hl.spark.core.req.service.HotCategoryAnalysisTop10Service
import com.hl.summer.framework.core.TController

/**
 * 描述: 控制层
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:46
 **/
class HotCategoryAnalysisTop10Controller extends TController{

  private val service = new HotCategoryAnalysisTop10Service

  override def execute() {
    val result = service.analysis()
    result.foreach(println)
  }
}
