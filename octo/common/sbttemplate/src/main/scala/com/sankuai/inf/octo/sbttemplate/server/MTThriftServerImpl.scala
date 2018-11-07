package com.sankuai.inf.octo.sbttemplate.server

import com.sankuai.inf.octo.scalatemplate.HelloService

/**
 * Created by chenxi on 9/15/15.
 */


class MTThriftServerImpl extends HelloService.Iface {
  def hi() = "yo"
}
