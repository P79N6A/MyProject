package com.sankuai.octo.log.codec

import io.netty.channel.ChannelHandler.Sharable
import io.netty.handler.codec.LengthFieldPrepender

/**
  * Created by wujinwu on 16/4/26.
  */
@Sharable
object IntLengthFieldPrepender extends LengthFieldPrepender(4)