package com.sankuai.octo

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by wangyanzhao on 15/1/18.
 */
class scalacollection extends FunSuite with BeforeAndAfter {
  test("List.map") {
    val listInt = List(1,2,3)
    listInt.map(_ * 2)
    //List(2,4,6)
    val listString = List("w","y","z")
    listString.map(_.toUpperCase)
    //List("W","Y","Z")
    val list = List("wyz", 23, "18511693743")
    val name = {
      a:Any => println(" name: " + a)
    }
    val age = {
      a:Any => println(" age: " + a)
    }
    val tel = {
      a:Any => println(" tel: " + a)
    }
    val funList = List(name, age, tel)
    funList.zip(list).foreach(x => x._1(x._2))

    (0 until funList.length).foreach(x => funList(x)(list(x)))
  }

  test("List.groupBy") {
    case class stu(name:String, sex:String, age:Int)
    val classMates = List(
      stu("a","male",23),
      stu("b","male",21),
      stu("c","female",20),
      stu("d","male",26),
      stu("e","female",24),
      stu("f","male",23),
      stu("g","male",22),
      stu("h","female",22),
      stu("i","male",23),
      stu("j","male",25),
      stu("k","male",26),
      stu("l","female",25),
      stu("m","female",26),
      stu("n","male",20),
      stu("o","male",24),
      stu("p","female",22),
      stu("r","male",23)
      )
    val result = classMates.groupBy(x => x.sex).map(x => (x._1,x._2.groupBy(_.age).map(x => (x._1,x._2.length))))
    println(result)
  }
}
