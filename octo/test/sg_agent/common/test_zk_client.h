#ifndef _TEST_ZK_CLIENT_H_
#define _TEST_ZK_CLIENT_H_

#include <zookeeper/zookeeper.h>
#include <string.h>
#include <vector>
#include <iostream>
#include "cJSON.h"
#include "sgagent_service_types.h"
using namespace std;

class ZkClientOperation
{
    public:
        ZkClientOperation();
        ~ZkClientOperation();

        int createOctoTree();

        int init(const string& appkey, const string& server);
        int deinit();
        int setZNode(const SGService& oservice);
        int getZNode(vector<SGService> &serviceList);
        int deleteZNode(const SGService& oservice);
        
        int checkZk();

        int setProvider(const CProviderNode &oprovider);
        int getProvider(CProviderNode &oprovider);

        int setRoute(const CRouteData &oroutedata);
        int deleteRoute(const CRouteData &oroutedata);
    private:
        int closeZk();
        int connectToZk();

        static void connWatcher(zhandle_t *zh, int type, int state, const char* path, void *watcher_ctx);

        int _SGService2Json(const SGService& oservice, std::string& strJson);
        int _Json2SGService(const std::string& strJson, SGService& oservice);
        int _ProviderNode2Json(const CProviderNode& oprovider, std::string& strJson);
        int _Json2ProviderNode(const std::string& strJson, int mtime, int version, int cversion, CProviderNode& oprovider);
        int _CRouteData2Json(const CRouteData& routedata, std::string& strJson);
    private:
        zhandle_t * zk_handler_;
        string server_;
        int timeout_;
        string base_zkpath_;
        string appkey_;
};

#endif
