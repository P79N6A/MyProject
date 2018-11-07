#ifndef __HLB_DYUPS_DATA_MANAGER_H__
#define __HLB_DYUPS_DATA_MANAGER_H__

#include <boost/shared_ptr.hpp>
#include "./utils/Singleton.h"
#include "./utils/auto_lock.h"
#include "./hlb_gen_cpp/SGAgent.h"
#include "./SgAgentClient.h"

namespace inf {
namespace hlb {
using namespace std;

class DyupsDataManager : public Singleton< DyupsDataManager> {
public:
    DyupsDataManager() {
        _pulling_interval = 5;
    }
    bool initialization();
    
    //定期拉取sg_agent数据，并调用diff函数做决策
    void DyupsDataPullTimer();
    
    //落地配置文件
    int dypusDataToFile();

public:
    string getUpstreamString (const string& appkey,
                              const vector<SGService>& httpServiceList,
                              const HttpProperties& prop);
    
    //===============================================================
    void getHttpServiceDiff (int bizCode,
                             const map< string, vector< SGService> >& newHttpServiceListMap,
                             map< string, vector< SGService> >& diffHttpServiceListMap);
    bool isVectorSGServiceDifference (const vector< SGService>& v_a,
                                      const vector< SGService>& v_b);
    void tripInvalidSGService (vector< SGService>& serviceList);
    
    string printSGServiceVector (const string& prefix, const vector< SGService>& v);
    string printSGServiceMap (const string& prefix, const map<string, vector< SGService> >& m);
    
    vector<SGService> getServiceListByAppkey (const string& appkey);

    //================================================================
    void getHttpPropertiesDiff (int bizCode,
                                const map< string, HttpProperties>& newPropertiesMap,
                                map< string, HttpProperties>& diffPropertiesMap);
    bool isPropertiesDifference (const HttpProperties& p_a,
                                 const HttpProperties& p_b);
    
    string printPropertiesMap( const string& prefix, const map<string, HttpProperties>& m);
    
    HttpProperties getPropertiesByAppkey (const string& appkey);
    
private:
    //轮询sg_agent的时间间隔
    int _pulling_interval;

    //业务线bizCode
    vector<int> _biz_code_vec;

    //存储appkey下的所有service节点
    map< int, map< string, vector<SGService> > > _http_service_list_map;   //key为bizCode
    //TODO 应该用读写锁
    Lock _service_list_mutex;
    
    //存储appkey下的所有http-properties
    map< int, map< string, HttpProperties> > _http_properties_map; //key为bizCode
    //TODO 应该用读写锁
    Lock _properties_mutex;
    
    //获取sg_agent数据
    boost::shared_ptr<SgAgentClientCollector> _sgagent_client_controller;

};

}
}

#endif  //__HLB_DYUPS_DATA_MANAGER_H__
