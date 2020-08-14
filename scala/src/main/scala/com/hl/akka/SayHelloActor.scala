package com.hl.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
 * 描述: 
 * 作者: panhongtong
 * 创建时间: 2020-08-13 16:07
 **/
class SayHelloActor extends Actor{

  // type Receive = PartialFunction[Any, Unit]
  override def receive: Receive = {
    case "hello" => println("hello啊~~~  :)")
    case "ok" => println("ok~~~  :)")
    case _ => println("啥都没  :(")
  }
}

object SayHelloActorDemo {

  // 创建一个 actorsystem ，用于创建actor
  val actorFactory = ActorSystem("actoryFactory")

  // 创建一个Actor的同时返回一个ActorRef
  // Props[SayHelloActor]：采用反射的方式创建一个 SayHelloActor 实例
  private val sayHelloActorRef:ActorRef =  actorFactory.actorOf(Props[SayHelloActor],"sayHelloActor")

  def main(args: Array[String]): Unit = {
    sayHelloActorRef ! "hello"
  }
}
