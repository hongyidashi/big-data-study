package com.hl.summer.framework.core

import com.hl.summer.framework.util.EnvUtil

trait TDao {

  def readFile(path:String) = {
    EnvUtil.getEnv().textFile(path)
  }
}