package com.hl.spark.core.req.controller

import com.hl.spark.core.req.service.PageFlowService
import com.hl.summer.framework.core.TController

/**
 * 描述: 
 * 作者: panhongtong
 * 创建时间: 2020-08-24 14:15
 **/
class PageFlowController extends TController{

  private val pageFlowService = new PageFlowService

  override def execute(): Unit = {
    pageFlowService.analysis()
  }
}
