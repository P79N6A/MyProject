#ifndef __registe_zkclient__H__
#define __registe_zkclient__H__

#include <iostream>
#include <map>
#include <set>
#include <list>

#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"
#include "zk_client.h"

#include "version_operation.h"

namespace sg_agent {

class RegisteZkClient {
public:
    RegisteZkClient();
    ~RegisteZkClient();

    int RegisterService(const SGService& oservice,
            RegistCmd::type regCmd = RegistCmd::REGIST,
            int uptCmd = UptCmd::RESET);

    //注册应用信息到ZK
    int RegisterServiceToZk(const SGService& oservice,
            RegistCmd::type regCmd = RegistCmd::REGIST,
            int uptCmd = UptCmd::RESET);

    //注册IP：PORT节点
    int RegisterServiceNodeToZk(const SGService& oservice,
            RegistCmd::type regCmd = RegistCmd::REGIST,
            int uptCmd = UptCmd::RESET);

    //注册servicename -> appkey映射关系到ZK
    int RegisteServiceNameToZk(const SGService& oservice, int);
    int EditServiceName(SGService&, const SGService&, int);

    bool CheckLegal(const SGService&);
    bool IsLimitOnZk(const SGService&);
    bool CheckLegalOnOps(const SGService&);

private:
    const int retry_;  
    VersionOperation version_;  
};
}

#endif

