package com.hl.abs_control

/**
 * 描述: 抽象控制
 * 作者: panhongtong
 * 创建时间: 2020-08-12 17:47
 **/
object AbsControlDemo {
  def main(args: Array[String]): Unit = {

    def myRunInThread(f: => Unit) = {
      new Thread {
        override def run(): Unit = {
          f
        }
      }.start()
    }

    myRunInThread {
      println("给爷抽象")
      Thread.sleep(3000)
      println("爷抽象完了")
    }

  }
}
