//
// Created by root on 8/15/17.
//

#ifndef CRANE_SRC_CSERVER_H
#define CRANE_SRC_CSERVER_H


//#include <muduo/net/TcpServer.h>
#include "cr_server_factory.h"
#include "../protocol/CmdTypes.h"
#include "../../comm/cJSON.h"
class CraneAgentServer : public Server {
 public:
  CraneAgentServer() {}
  ~CraneAgentServer() {};

  void start() {};
  rt_code_type onMessage(int fd, char *buffer);

 private:
  unsigned int BkdrHash(const char *key);
};

class CraneHttpServer : public Server {
 public:
  CraneHttpServer() {};
  ~CraneHttpServer() {};

  void start(){};
  rt_code_type onMessage(int fd, char *buffer){};

 private:
  unsigned int BkdrHash(const char *key);
};
class CraneUdpServer : public Server {
 public:
  CraneUdpServer() {};
  ~CraneUdpServer() {};
 void start(){};
  rt_code_type onMessage(int fd,char *buffer){};

 private:
  unsigned int BkdrHash(const char *key);
};
#endif //CRANE_SRC_CSERVER_H
