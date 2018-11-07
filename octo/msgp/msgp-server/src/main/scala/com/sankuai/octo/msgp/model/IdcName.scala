package com.sankuai.octo.msgp.model

/**
  * Created by yves on 16/8/5.
  */
object IdcName {

  val idcNameMap = Map(
    "YF" -> "永丰",
    "CQ" -> "次渠",
    "DX" -> "大兴",
    "RZ" -> "润泽",
    "JX" -> "酒仙桥",
    "GH" -> "光环",
    "GQ" -> "桂桥",
    "YP" -> "月浦",
    "XH" -> "徐汇",
    "CD" -> "成都",
    "SJZ" -> "石家庄",
    "HJ" -> "邗江",
    "NH" -> "南汇",
    "BS" -> "宝山",
    "YY" -> "易园",
    "SB" -> "市北",
    "TX_HK1" -> "香港一区",
    "TX_GZ1" -> "广州一区",
    "TX_DLD1" -> "多伦多一区",
    "TX_SH1" -> "上海一区",
    "TX_BJ1" -> "北京一区",
    "TX_BJ2" -> "北京二区",
    "TX_GZ3" -> "广州三区",
    "TX_GZ2" -> "广州二区",
    "TX_GZ4" -> "广州四区",
    "TX_CD2" -> "成都二区",
    "GHA" -> "光环A",
    "TS" -> "唐山",
    "DG" -> "东莞",
    "JD" -> "嘉定",
    "TX_BJ3" -> "北京三区",
    "ZW" -> "中卫",
    "TENCENT" -> "腾讯云（点评）",
    "OTHER" -> "其他"
  )

  def getNameByIdc(idc: String) = {
    idcNameMap.getOrElse(idc,"其他")
  }

  def getIdcByName(name: String) = {
    val default = ("OTHER",name)
    idcNameMap.find(_._2 == name).getOrElse(default)._1
  }
}
