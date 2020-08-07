package com.hl

package scalapackage {

  class PackageClazz {

  }

  package object packageobj {
    val name = "package"
    def sayPackage() {
      println("Hi")
    }
  }

  package packageobj {
    object TestPackage{
      def main(args: Array[String]): Unit = {
        println("name:" + name)
        sayPackage()
      }
    }
  }

}


