package com.sankuai.octo.mworth.service

import java.util.Calendar

import org.slf4j.LoggerFactory

/**
 * Created by zava on 15/12/14.
 */
object mWorthService {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
    1: 计算季度的时间
    2: 统计每个季度的同一个project 下的模块
    3: 统计模块下的价值
  */
  def report(project: String, model: String) = {

  }
  //1: 计算季度的时间
  def getQTime() = {
    val arr_Q = new Array[Long](5)
    //获取Q1,Q2,Q3,Q4,的时间
    val calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.clear(Calendar.MILLISECOND)
    calendar.set(Calendar.MONTH,0)
    arr_Q(0) = calendar.getTime.getTime
    calendar.set(Calendar.MONTH, 3)
    arr_Q(1) = calendar.getTime.getTime
    calendar.set(Calendar.MONTH, 6)
    arr_Q(2) = calendar.getTime.getTime

    calendar.set(Calendar.MONTH, 9)
    arr_Q(3) = calendar.getTime.getTime
    calendar.add(Calendar.YEAR, 1)
    calendar.set(Calendar.MONTH, 0)
    arr_Q(4) = calendar.getTime.getTime
    arr_Q
  }
}
