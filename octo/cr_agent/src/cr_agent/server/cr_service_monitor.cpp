//
// Created by smartlife on 2017/8/7.
//
#include "cr_service_monitor.h"

void CraneServiceMonitor::CmdServiceCheck()
{
	if(CAgentService::GetCSInstance()->CServiceMonitor()<0){

      LOG_INFO("nothing to removed");
	} else{
      LOG_INFO(" monitor has removed the dead cmd");
    }
}
int CraneServiceMonitor::StartServiceMonitor(){

  struct timeval tv;
  int schTime=CAgentService::GetCSInstance()->GetSchTimeOut();
	LOG_INFO("the sch time"<<schTime);

	muduo::net::EventLoop loop;
	loop.runEvery(schTime, boost::bind(&CraneServiceMonitor::CmdServiceCheck, this));
  loop.loop();

  return ERROR_RETURN;
}
