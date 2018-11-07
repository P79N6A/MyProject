package com.sankuai.octo.log.utils

import io.netty.channel.nio.NioEventLoopGroup

/**
  * Created by wujinwu on 16/4/22.
  */
object EventLoopUtil {

  private val eventLoopGroups = (1 to Runtime.getRuntime.availableProcessors()).map { _ => new NioEventLoopGroup() }
  private var iter = eventLoopGroups.iterator

  def nextEventLoopGroup = {
    if (iter.hasNext) {
      iter.next()
    } else {
      iter = eventLoopGroups.iterator
      iter.next()
    }
  }

}
