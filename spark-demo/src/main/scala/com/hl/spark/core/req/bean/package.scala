package com.hl.spark.core.req

package object bean {
  case class HotCategory(var categoryId:String,var clickCount:Int,
                         var orderCount:Int,var payCount:Int)
}
