package com.hl.summer.framework.core

import java.net.{ServerSocket, Socket}

import com.hl.summer.framework.util.EnvUtil

trait TApplication {

  var envData: Any = null

  /**
   * 开发原则： OCP Open - Close (开闭原则)
   * Open : 对功能扩展开放
   * Close: 在扩展的同时不应对原有代码进行修改
   */

  /**
   * 启动应用
   * 1. 函数柯里化
   * 2. 控制抽象
   *
   * t 参数, 类型： jdbc, file, hive, kafka, socket, serverSocket
   */
  def start(t: String = "jdbc")(op: => Unit): Unit = {
    // 1. 初始化环境
    if (t == "socket") {
      envData = new Socket()
    } else if (t == "serverSocket") {
      envData = new ServerSocket()
    } else if (t == "spark") {
      envData = EnvUtil.getEnv()
    }

    // 2. 业务逻辑
    try {
      op
    } catch {
      case ex: Exception => println("业务执行失败 ：" + ex.getMessage)
    }

    // 3. 关闭环境
    if (t == "socket") {
      val socket: Socket = envData.asInstanceOf[Socket]
      if (!socket.isClosed) {
        socket.close()
      }
    } else if (t == "serverSocket") {
      val serverSocket: ServerSocket = envData.asInstanceOf[ServerSocket]
      if (!serverSocket.isClosed) {
        serverSocket.close()
      }
    } else if (t == "spark") {
      EnvUtil.clear()
    }
  }
}