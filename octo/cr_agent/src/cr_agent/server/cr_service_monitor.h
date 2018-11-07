//
// Created by smartlife on 2017/8/7.
//


#ifndef CRANE_AGENT_CR_SERVICE_MONITOR_H
#define CRANE_AGENT_CR_SERVICE_MONITOR_H

#include <muduo/net/EventLoop.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/time.h>
#include "../../comm/log4cplus.h"
#include "cr_service.h"
#include "../init/cr_agent_conf.h"
#define MAX_SCHEDULE_TIME 1
#define RUN_AGENT_MONITOR 1
/*
 *监视线程用于检测是否有
 *异常或dead操作命令
 *根据检查合法性进行清理
 * */
class CraneServiceMonitor{

 public:

  CraneServiceMonitor(){};
  ~CraneServiceMonitor(){};
  int StartServiceMonitor();

 private:

  void CmdServiceCheck();
};



#endif //CRANE_AGENT_CR_SERVICE_MONITOR_H
