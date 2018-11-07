//
// Created by root on 8/16/17.
//

#include "cr_server_factory.h"
#include "cr_server.h"

/*
 * 根据创建的server type类型创建不同的server类型
 * */
boost::shared_ptr<Server> ServerFactory::CreateServer(SERVER_TYPE svrType) {
  switch (svrType) {

    case CRANE_TCP_SERVER: {
      boost::shared_ptr<CraneAgentServer> craneTcpServer
          = boost::make_shared<CraneAgentServer>();
      return craneTcpServer;
    }
    case CRANE_HTTP_SERVER: {
      boost::shared_ptr<CraneHttpServer> craneHttpServer
          = boost::make_shared<CraneHttpServer>();
      return craneHttpServer;
    }
    case CRANE_UDP_SERVER: {
      boost::shared_ptr<CraneUdpServer> craneUdpServer
          = boost::make_shared<CraneUdpServer>();
      return craneUdpServer;
    }
    default: {
      break;
    }
  }
  return boost::shared_ptr<Server>();
}
