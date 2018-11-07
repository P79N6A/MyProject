package com.sankuai.octo

import java.io.{BufferedInputStream, IOException}

import com.sankuai.inf.octo.mns.MnsInvoker
import com.sankuai.meituan.config.FileConfigClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by lhmily on 11/08/2015.
 */
@RunWith(classOf[JUnitRunner])
class mccSuite extends FunSuite with BeforeAndAfter {
  test("mcc") {
    MnsInvoker.getInstance("10.4.244.156:5266")
//    println(MnsInvoker.getInstance)
    val client: FileConfigClient = new FileConfigClient
    client.setAppkey("com.sankuai.octo.tmy")
    client.init
    val stream: BufferedInputStream = client.getFile("test.conf")
    printFile(stream)


//    var transport: TTransport = null
////        var result: List[SGService] = null
//    try {
//      val timeout = 3000
//      transport = new TFramedTransport(new TSocket("10.4.244.156", 5266, timeout), 16384000)
//      val protocol: TProtocol = new TBinaryProtocol(transport)
//      val agent = new SGAgent.Client(protocol)
//      transport.open
//      val param = new file_param_t("com.sankuai.inf.sg_agent", "", "10.4.244.156")
//      param.env = "prod"
//      println(param)
//      val data = agent.getFileConfig(param)
//      println(data)
//
//    } catch {
//      case e: Exception =>
//        e.printStackTrace()
//    } finally {
//      if (null != transport) {
//        try {
//          transport.close()
//        } catch {
//          case e: Exception =>
//        }
//      }
//    }
  }

  //  test("change") {
  //    MnsInvoker.getInstance("10.4.244.156:5266");
  //    val client: FileConfigClient = new FileConfigClient
  //    client.setAppkey("com.sankuai.inf.sg_agent")
  //    client.init
  //    val stream: BufferedInputStream = client.getFile("test.conf")
  //    printFile(stream)
  //    client.addListener("test.conf", new FileChangeListener() {
  //      def changed(fileName: String, oriFile: BufferedInputStream, newFile: BufferedInputStream) {
  //        try {
  //          System.out.println(fileName)
  //          printFile(oriFile)
  //          printFile(newFile)
  //        }
  //        catch {
  //          case e: IOException => {
  //            e.printStackTrace
  //          }
  //        }
  //      }
  //    })
  //    new Thread() {
  //      override def run {
  //        while (true) {
  //          try {
  //            val stream: BufferedInputStream = client.getFile("test.conf")
  //            printFile(stream)
  //            Thread.sleep(1000)
  //          }
  //          catch {
  //            case e: Exception => {
  //              e.printStackTrace
  //            }
  //          }
  //        }
  //      }
  //    }.start
  //    Thread.sleep(6000)
  //  }

  @throws(classOf[IOException])
  def printFile(stream: BufferedInputStream) {
    val buffer: Array[Byte] = new Array[Byte](1000)
    stream.read(buffer, 0, 1000)
    val str: String = new String(buffer)
    println(s"文件内容:$str")
  }

}
