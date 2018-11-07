package com.sankuai.octo

import java.util.concurrent.ConcurrentHashMap

import scala.compat.Platform

/**
 * Created by xintao on 15/10/3.
 */
object AlarmNumTest {
    def main(args: Array[String]) {
      val lastAlarmTimeMap = new ConcurrentHashMap[String, Long]()
      while (true){
        println("Please enter a num: ");
        val line = Console.readLine()
        val currentTimestamp = Platform.currentTime
        if (lastAlarmTimeMap.containsKey(line)) {
          val lastTimestamp = lastAlarmTimeMap.get(line)
          val timeDiff = (currentTimestamp - lastTimestamp) / 1000
          println("lastAlarmTimeMap timeDiff:"+timeDiff)
          //据上次报警>30分钟 报警
          if (timeDiff > 60) {
            println(">30 minutes,alarm")
            println(line,currentTimestamp)
            lastAlarmTimeMap.put(line,currentTimestamp)
          }
          //30分钟以内 且连续报警四次以上 退出 不报警
          else if (timeDiff >  30){
            println("In 30 minutes,alarm num >= 4,no alarm")
          } else {
          println("In 30 minutes,alarm num < 4,alarm")
          println(line,currentTimestamp)
        }} else {
          println("first overload,alarm")
          println(line, currentTimestamp)
          lastAlarmTimeMap.put(line, currentTimestamp)
        }
      }
    }
}
