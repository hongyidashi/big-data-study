package com.hl.spark.core.req.application

import com.hl.spark.core.req.controller.PageFlowController
import com.hl.summer.framework.core.TApplication

/**
 * 描述: 页面单跳转换率统计
 * 作者: panhongtong
 * 创建时间: 2020-08-24 14:16
 **/
object PageFlowApplication extends App with TApplication {

  start("spark") {
    val controller = new PageFlowController
    controller.execute()
  }
}
