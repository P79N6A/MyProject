//
// Created by smartlife on 2017/8/12.
//
#ifndef SG_AGENT2_CPLUGININIT_H
#define SG_AGENT2_CPLUGININIT_H
#include <sys/prctl.h>
#include <pthread.h>
#include <unistd.h>
#include <iostream>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string>
#include <vector>
#include <map>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/epoll.h>
#include <errno.h>
#include "cr_agent_conf.h"
#include <fcntl.h>
#include "../../plugin/plugindef.h"
#include "../../util/cr_common.h"
#include "../server/cr_service.h"
#include "../server/cr_server.h"
#include "../server/cr_server_factory.h"
#include "../../comm/log4cplus.h"
#include "cr_agent_init.h"
#include "../server/cr_service_monitor.h"
#define OPEN_MAX 100
#define MAXSIZE 1024
//todo:init the cplugin want to load interface

typedef void *HANDLE;

const std::string LOG_SERVER_CONF = "/opt/meituan/apps/cr_agent/log4cplus.conf";
class CCInterface {
public:

    ~CCInterface();
    static CCInterface* GetCCInstance();
    int GetLoadedByModuleType(LOADED_MODULE_TYPE cLoadType);
    int FreeLoadedByModuleType(LOADED_MODULE_TYPE cLoadType);

    CraneAgentConf* GetConfigIns(){
            return &m_LoadConfig;
    }

private:
    CCInterface();
    CraneAgentConf m_LoadConfig;
    static pthread_mutex_t m_CMutex;
    static CCInterface* m_CCInterfaceInstance;
protected:
};

#endif //SG_AGENT2_CPLUGININIT_H
