package com.sankuai.msgp.common.service.org

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.auth.vo.User
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import org.apache.commons.lang3.StringUtils
import org.slf4j.{Logger, LoggerFactory}

object UserService {
  private val log: Logger = LoggerFactory.getLogger(UserService.getClass)
  private val noCheckUserKey = "api.user.not_check"

  private var noCheckUserSet: Set[String] = {
    val value = MsgpConfig.get(noCheckUserKey)
    MsgpConfig.addListener(noCheckUserKey, new IConfigChangeListener() {
      def changed(key: String, oldValue: String, newValue: String) = {
        log.info(s"$noCheckUserKey new value=" + newValue)
        noCheckUserSet = getNoCheckUserSet(newValue)
      }
    })
    getNoCheckUserSet(value)
  }

  def getNoCheckUserSet(noCheckUserVal: String): Set[String] = {
    noCheckUserVal.split(",").toSet.map((x: String) => x.toLowerCase)
  }

  def bindUser(username: String) = {
    val optUser = if (StringUtils.isBlank(username)) {
      None
    } else if (!noCheckUserSet.contains(username.toLowerCase)) {
      SsoService.getUser(username)
    } else {
      None
    }

    val bindUser = if (optUser.isEmpty) {
      val user: User = new User
      user.setLogin(username)
      user.setName(username)
      user.setId(1)
      user
    }
    else {
      optUser.get
    }
    UserUtils.bind(bindUser)
    bindUser
  }
}
