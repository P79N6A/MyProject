//
// Created by root on 8/15/17.
//
#include "cr_server.h"
#include "cr_service.h"

//rcv msg dispatch

rt_code_type CraneAgentServer::onMessage(int fd, char *buffer) {
  if(fd<0||NULL==buffer){
    LOG_ERROR("==fd"<<fd<<"buffer="<<buffer<<"==");
    return (rt_code_type)ERROR_RETURN;
  }
  const int tmpSvcType = COMMAND_SERVICE;

  switch (tmpSvcType) {
    case COMMAND_SERVICE: {
     return (rt_code_type) CAgentService::GetCSInstance()->CServiceOnMessage(fd, buffer);
    }
    case DEPLOY_SERVICE: {
      return (rt_code_type) CAgentService::GetCSInstance()->CDeployOnMessage(fd, buffer);
    }
    case LOG_SERVICE: {
      return (rt_code_type) CAgentService::GetCSInstance()->CLogOnMessage(fd, buffer);
    }
    default: {
      return (rt_code_type) CAgentService::GetCSInstance()->CDefaultMessage(fd, buffer);
    }
  }
  return (rt_code_type)ERROR_CODE_NULL;
}










