//
//  OriginalUpstreamManager.h
//  HlbManager
//
//  Created by zhangjinlu on 15/11/19.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//
#ifndef __HLB_ORIGINAL_UPSTREAM_MANAGER_H__
#define __HLB_ORIGINAL_UPSTREAM_MANAGER_H__

#include <boost/shared_ptr.hpp>
#include "./utils/Singleton.h"
#include "./utils/auto_lock.h"
#include "./hlb_gen_cpp/SGAgent.h"
#include "./hlb_gen_cpp/MNSCacheService.h"
#include "./SgAgentClient.h"
#include "./MNSCacheClient.h"
#include "OriginalUpstreamKeyTab.h"

namespace inf {
namespace hlb {
using namespace std;
    
typedef struct _server_node {
    string _ip;
    int _port;
    int _fail_timeout;
    int _max_fails;
    int _slow_start;
    int _weight;
} ServerNode;
    
class OriginalUpstreamManager : public Singleton< OriginalUpstreamManager> {
public:
    OriginalUpstreamManager():_is_grey(0), _pulling_interval(5),
                            _nginx_type(""), _idc_type("") {
    }
    bool initialization();
    
    //定期拉取MNSC数据，并调用diff函数做决策
    void OriginalUpstreamPullTimer();
    
    //TODO落地配置文件
    int OriginalUpstreamToFile(const string& upstreamName,
                               const UpstreamContent& content);
public:
    void getUpstreamDiff(const map<string, string>& newUpstreamMap,
                         map<string, string>& diffUpstreamMap);
    
    void getUpstreamContentMap(const map<string, string>& jsonUpstreamMap,
                               map<string, UpstreamContent>& contentMap);
    bool enableUpdate (const string& upstreamName,
                       const string& upstreamJsonStr);
    bool isUpstreamJsonStrValid (const string& upstreamName,
                                 const string& upstreamJsonStr);
    bool isUpstreamJsonStrDiff (const string& up_a, const string& up_b);
    bool isServerNodeVecDiff( vector<ServerNode>& v_a, vector<ServerNode>& v_b);
    
private:
    //节点状态信息
    int _is_grey;
    string _idc_type;
    string _nginx_type;
    
    //轮询MNSC的时间间隔
    int _pulling_interval;
    
    //Json串关键字
    OriginalUpstreamKeyTab _key_tab;
    
    //缓存upstream，map< upstreamName, upstreamJsonValue>
    map< string, string> _json_upstream_map;
    //TODO 应该用读写锁
    Lock _json_upstream_mutex;
    
    //MNSC client
    boost::shared_ptr<MNSCacheClientCollector> _mnsc_client_controller;
};
    
}
}
#endif //__HLB_ORIGINAL_UPSTREAM_MANAGER_H__
