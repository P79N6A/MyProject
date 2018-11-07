#ifndef _TEST_SG_AGENT_H_
#define _TEST_SG_AGENT_H_

#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>
#include <string.h>
#include <iostream>
#include <vector>
#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>

#include "SGAgent.h"
#include "sgagent_service_types.h"
#include "sgagent_common_types.h"
#include "quota_common_types.h"

#include <arpa/inet.h>
#include <ifaddrs.h>

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace boost;
const int MAX_INTERFACE_NUM = 32;

class SGAgentHandler
{
    public:
        int init(const string &remoteAppkey, const string &ip, int port);
        int deinit();
        int getServiceList(vector<SGService> &sgserviceList);
        int registerService(SGService &service);
        int registeServicewithCmd(int cmd, SGService &service);
        int unRegisterService(SGService &service);
        int uploadModuleInvoke(SGModuleInvokeInfo &oInfo);
        int uploadLog(SGLog &oLog);

        SGAgentClient *client_;

        void PrintServiceList(const std::vector<SGService> &serviceList);
        bool isInServiceList(vector<SGService>& servicelist, const SGService &service);

        inline int getIntranet(char ip[INET_ADDRSTRLEN], char mask[INET_ADDRSTRLEN]) {
            int ret = 0;

            struct ifaddrs* ifAddrStruct = NULL;
            struct ifaddrs* ifa = NULL;
            void* tmpAddrPtr = NULL;
            void* tmpMaskPtr = NULL;
            char addrArray[MAX_INTERFACE_NUM][INET_ADDRSTRLEN];
            char maskArray[MAX_INTERFACE_NUM][INET_ADDRSTRLEN];
            getifaddrs(&ifAddrStruct);
            int index = 0;
            for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa -> ifa_next)
            {
                if ((NULL == ifa->ifa_addr) || (0 == strcmp(ifa->ifa_name, "vnic")))
                {
                    continue;
                }
                if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
                    tmpAddrPtr = &((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
                    inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
                    if(0 == strcmp(addrArray[index], "127.0.0.1")) {
                        continue;
                    }

                    tmpMaskPtr = &((struct sockaddr_in *)ifa->ifa_netmask)->sin_addr;
                    inet_ntop(AF_INET, tmpMaskPtr, maskArray[index], INET_ADDRSTRLEN);

                    strcpy(ip, addrArray[index]);
                    strcpy(mask, maskArray[index]);
                    if(++index > MAX_INTERFACE_NUM - 1) {
                        break;
                    }

                }
            }

            if(index > 1) {
                int idx = 0;
                while(idx < index) {
                    if(NULL != strstr(addrArray[idx], "10.")
                            && 0 == strcmp(addrArray[idx],
                                strstr(addrArray[idx], "10."))) {
                        strcpy(ip, addrArray[idx]);
                        strcpy(mask, maskArray[idx]);
                    }
                    idx++;
                }
            }
            else if (0 >= index){
                ret = -10;
            }

            if (ifAddrStruct != NULL) {
                 freeifaddrs(ifAddrStruct);
            }
            return ret;
        }

        
    private:
        int checkConnection();
        int closeConnection();
        

    private:
        string localAppkey_;
        string remoteAppkey_;
        string ip_;
        int port_;
        shared_ptr<TSocket> socket_;
        shared_ptr<TTransport> transport_;
        shared_ptr<TProtocol> protocol_;
};
#endif
