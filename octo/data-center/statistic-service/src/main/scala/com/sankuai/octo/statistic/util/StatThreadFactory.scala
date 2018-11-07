package com.sankuai.octo.statistic.util

import org.springframework.scheduling.concurrent.CustomizableThreadFactory

/**
  * Created by wujinwu on 15/12/29.
  */
object StatThreadFactory {

  def threadFactory[T](clazz: Class[T]): CustomizableThreadFactory = {
    threadFactory(clazz.getSimpleName)
  }

  def threadFactory(prefix: String): CustomizableThreadFactory = {
    new CustomizableThreadFactory(s"$prefix-thread-")
  }
}
