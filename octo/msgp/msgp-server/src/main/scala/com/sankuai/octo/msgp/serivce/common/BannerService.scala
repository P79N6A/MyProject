package com.sankuai.octo.msgp.serivce.common

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.octo.msgp.dao.common.BannerDAO

object BannerService {

  case class BannerMessage(messageType: Int, messageTitle: String, messageBody: String)

  def deleteBannerMsg(messageType: Int, messageTitle: String, messageBody: String) = {
    val currentUser = UserUtils.getUser
    val username = currentUser.getName
    val expired = false
    BannerDAO.deleteMessage(BannerDAO.BannerDomain(messageType, messageTitle, messageBody, username, expired))
  }

  def updateBannerMsg(messageType: Int, messageTitle: String, messageBody: String) = {
    val currentUser = UserUtils.getUser
    val username = currentUser.getName
    val expired = false
    BannerDAO.updateMessage(BannerDAO.BannerDomain(messageType, messageTitle, messageBody, username, expired))
  }

  def insertBannerMsg(messageType: Int, messageTitle: String, messageBody: String) = {
    val currentUser = UserUtils.getUser
    val username = currentUser.getName
    val expired = false
    val create_time = System.currentTimeMillis()
    BannerDAO.insertMessage(BannerDAO.BannerDomain(messageType, messageTitle, messageBody, username, expired, create_time))
  }

  def getBannerMsg = {
    val result = BannerDAO.getValidMessage
    val list = result.map{
      x=>
        BannerMessage(x.messageType, x.messageTitle, x.messageBody)
    }
    list
  }

  def getAllBannerMsg = {
    val result = BannerDAO.getAllMessage
    result.map{
      x=>
        BannerMessage(x.messageType, x.messageTitle, x.messageBody)
    }
  }
}
