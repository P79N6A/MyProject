//
// Created by root on 8/16/17.
//
#ifndef CRANE_SRC_SERVERFACTORY_H
#define CRANE_SRC_SERVERFACTORY_H

#include <stdio.h>
#include <boost/shared_ptr.hpp>
#include <boost/make_shared.hpp>
#include "../../util/cr_common.h"
#include "../protocol/CmdTypes.h"

class Server {
 public:

  Server() {};
  ~Server() {};

  virtual void start()=0;
  virtual rt_code_type onMessage(int fd, char *buffer)=0;

 private:

};

class ServerFactory {
 public:
  boost::shared_ptr<Server> CreateServer(SERVER_TYPE svrType);
  Server *CreateServerTest(SERVER_TYPE svrType);
  ~ServerFactory() {};
 private:

 protected:

};

#endif //CRANE_SRC_SERVERFACTORY_H
