#ifndef _TEST_ZK_CLIENT_H_
#define _TEST_ZK_CLIENT_H_

#include <zookeeper/zookeeper.h>
#include <string.h>
#include <vector>
#include <iostream>
#include "cJSON.h"

#include "gen-cpp/sgagent_types.h"
using namespace std;

class CProviderNode
{
public:
    std::string appkey;   /// 服务唯一标识
    int lastModifiedTime;  /// 最近更改时间
    int mtime;                    //zk节点对应的mtime状态信息
    int cversion;                    //zk节点对应的cversion状态信息, 用来比对版本信息
    int version;                    //zk节点对应的version状态信息, 用来比对版本信息
};

class ZkClientOperation
{
    public:
        ZkClientOperation();
        ~ZkClientOperation();

        int init(const string& appkey);
        int deinit();
        int setZNode(const SGService& oservice);
        int getZNode(vector<SGService> &serviceList);
        int deleteZNode(const SGService& oservice);
        
        int checkZk();

        int setProvider(const CProviderNode &oprovider);
        int getProvider(CProviderNode &oprovider);

    private:
        int closeZk();
        int connectToZk();

        static void connWatcher(zhandle_t *zh, int type, int state, const char* path, void *watcher_ctx);

        int _SGService2Json(const SGService& oservice, std::string& strJson);
        int _Json2SGService(const std::string& strJson, SGService& oservice);
        int _ProviderNode2Json(const CProviderNode& oprovider, std::string& strJson);
        int _Json2ProviderNode(const std::string& strJson, int mtime, int version, int cversion, CProviderNode& oprovider);
    private:
        zhandle_t * zk_handler_;
        string server_;
        int timeout_;
        string base_zkpath_;
        string appkey_;
};

#endif
