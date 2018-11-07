// =====================================================================================
// 
//       Filename:  zk_client.h
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时12分19秒
//       Revision:  none
// 
// 
// =====================================================================================

#ifndef __json_zkclient__H__
#define __json_zkclient__H__

#include <iostream>
#include <map>
#include <set>
#include <list>

extern "C" {
#include "comm/cJSON.h"
}

#include "sgagent_service_types.h"

using namespace __gnu_cxx;

class JsonZkMgr {
public:

    static int ProviderNode2Json(const CProviderNode& oprovider, std::string& strJson);
    static int ServiceNameNode2Json(const std::string& serviceName,
        std::string& strJson);
    static int Json2ProviderNode(
        const std::string& strJson,
        unsigned long mtime,
        unsigned long version,
        unsigned long cversion,
        CProviderNode& oprovider);

    static int SGService2Json(const SGService& oservice, std::string& strJson, const int env_int);
    static int Json2SGService(const std::string& strJson, SGService& oservice);
    static int ServiceNode2Json(const ServiceNode& oservice, std::string& strJson);
    static int Json2ServiceNode(const std::string& strJson, ServiceNode& oservice);
    static int Json2RouteData(const std::string& strJson, CRouteData& orouteData);
    static int Json2RouteNode(const std::string& strJson,
        unsigned long mtime,
        unsigned long version,
        unsigned long cversion,
        CRouteNode& oroute);
		static int Json2AppkeyDescNode(const std::string& strJson,
				                                   AppkeyDesc& desc);

    static int cJson_AddStringArray(
             const std::set<std::string> &vecSvrname,
             cJSON* root, cJSON* item,
             const std::string& itemName);

    static int cJson_AddServiceObject(
            const std::map<std::string, ServiceDetail> &vecSvrname,
            cJSON* root, cJSON* item,
            const std::string& itemName);

    static int Json2DegradeActions(const std::string& strJson,
        std::vector<DegradeAction>& dActions);

    static cJSON* GetObjectItem(cJSON* item, const char* name);

};


#endif

