package com.hl.spark.core.req.dao

import com.hl.spark.core.req.bean.UserVisitAction
import com.hl.summer.framework.core.TDao

/**
 * 描述: Dao层
 * 作者: panhongtong
 * 创建时间: 2020-08-21 15:45
 **/
class HotCategorySessionAnalysisTop10Dao extends TDao{

  def getUserVisitAction(path: String) = {
    val fileRDD = readFile(path)
    fileRDD.map{line => {
      val data = line.split("_")
      UserVisitAction(
        data(0),
        data(1).toLong,
        data(2),
        data(3).toLong,
        data(4),
        data(5),
        data(6).toLong,
        data(7).toLong,
        data(8),
        data(9),
        data(10),
        data(11),
        data(12).toLong
      )
    }}
  }
}
