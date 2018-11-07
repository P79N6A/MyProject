//
//  DyupsDataManager.cpp
//  HbManager
//
//  Created by zhangjinlu on 15/11/2.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#include <iostream>
#include <vector>
#include <string>
#include <arpa/inet.h>
#include <boost/lexical_cast.hpp>
#include "./utils/log4cplus.h"
#include "Config.h"
#include "TengineTriger.h"
#include "DyupsDataManager.h"

using namespace std;
using namespace inf::hlb;

const string HEALTH_CHECK_KEY = "health_check";

bool DyupsDataManager::initialization() {
    _pulling_interval = 5; //默认拉取时间间隔为5秒
    _sgagent_client_controller = boost::shared_ptr<SgAgentClientCollector> (
                                        new SgAgentClientCollector());

    // 初始化sg_agent拉取时间间隔
    int pullingInterval = HlbConfig::instance().m_sgagentUpdateTime;
    if (pullingInterval>0) {
        _pulling_interval = pullingInterval;
    }

    //初始化bizCode列表
    vector<string>::const_iterator bizIter = (HlbConfig::instance().m_businessVec).begin();
    for( ; bizIter != (HlbConfig::instance().m_businessVec).end(); ++bizIter) {
        try {
            _biz_code_vec.push_back( boost::lexical_cast<int>(*bizIter) );
            LOG_INFO( "[initialization] bizcode = "<<_biz_code_vec.back());
        } catch (const std::exception& e) {
            LOG_ERROR( "[initialization] ERROR lexical_cast<int> for "<<*bizIter);
        }
    }
    if (_biz_code_vec.size() <= 0 ) {
        LOG_ERROR("[initialization] GET NO BIZCODE.");
        return false;
    }

    //初始化本地cache
    for( vector<int>::const_iterator bizIter = _biz_code_vec.begin() ;
         bizIter != _biz_code_vec.end() ; ++bizIter) {
        int bizCode = *bizIter;
        map< string, vector<SGService> > tmpServiceListMap;
        _http_service_list_map[bizCode] = tmpServiceListMap;
        map< string, HttpProperties> tmpPropertiesMap;
        _http_properties_map[bizCode] = tmpPropertiesMap;
    }

    return true;
}

//TODO:改用Timer实现
void DyupsDataManager::DyupsDataPullTimer() {
    LOG_INFO( "[DyupsDataPullTimer] start. _pulling_interval = "<< _pulling_interval);
    map< string, vector<SGService> > diffHttpServiceListMap;
    map< string, HttpProperties> diffHttpPropertiesMap;

    while(true) {
        diffHttpServiceListMap.clear();
        diffHttpPropertiesMap.clear();
        for( vector<int>::const_iterator bizIter = _biz_code_vec.begin() ;
             bizIter != _biz_code_vec.end() ; ++bizIter) {
            LOG_INFO("[DyupsDataPullTimer] gonna pull sg_agent for bizCode = "<<*bizIter);

            //1. 拉取http service list
            map< string, vector<SGService> > newHttpServiceListMap;
            _sgagent_client_controller->getHttpServiceListByBusinessLine( newHttpServiceListMap, *bizIter);
            
            //2. 比较新http service list与当前区别
            if (newHttpServiceListMap.size() >0) {
                getHttpServiceDiff( *bizIter, newHttpServiceListMap, diffHttpServiceListMap);
            }
            
            //3. 拉取http-properties
            map< string, HttpProperties> newHttpPropertiesMap;
            _sgagent_client_controller->getHttpPropertiesByBusinessLine( newHttpPropertiesMap, *bizIter);
            
            //4. 比较新http-properties与当前区别
            if (newHttpPropertiesMap.size() >0) {
                getHttpPropertiesDiff( *bizIter, newHttpPropertiesMap, diffHttpPropertiesMap);
            }
        }
        

        //5. 传入http-properties及http service list的diff，触发Tengine更新
        if (diffHttpServiceListMap.size()>0 || diffHttpPropertiesMap.size()>0) {
			if (diffHttpServiceListMap.size() >0) {
				LOG_INFO("[DyupsDataPullTimer] before combine, diffService.size="<< diffHttpServiceListMap.size() );
			}
			if (diffHttpPropertiesMap.size()>0) {
				LOG_INFO("[DyupsDataPullTimer] before combine, diffProperties.size="<< diffHttpPropertiesMap.size() );
            }
            
            //生成一个appkey的upstream需要完整的serviceList和properties，故补全之
            map< string, HttpProperties>::const_iterator propIter = diffHttpPropertiesMap.begin();
            for ( ; propIter!=diffHttpPropertiesMap.end() ; ++propIter) {
                string tmpAppKey = propIter->first;
                diffHttpServiceListMap[tmpAppKey] = getServiceListByAppkey(tmpAppKey);
            }
            map< string, vector<SGService> >::const_iterator servIter = diffHttpServiceListMap.begin();
            for ( ; servIter!=diffHttpServiceListMap.end() ; ++servIter) {
                string tmpAppKey = servIter->first;
                diffHttpPropertiesMap[tmpAppKey] = getPropertiesByAppkey(tmpAppKey);
            }
			
            //去掉不合法SGService
            map< string, vector<SGService> >::iterator tripServIter = diffHttpServiceListMap.begin();
            while (tripServIter!=diffHttpServiceListMap.end()) {
                string tmpAppKey = tripServIter->first;
                tripInvalidSGService(tripServIter->second);
                if ((tripServIter->second).size() <= 0) {
                    diffHttpServiceListMap.erase( tripServIter++);
                } else {
                    tripServIter++;
                }
            }

            LOG_INFO("[DyupsDataPullTimer] after combine, diffService.size="<< diffHttpServiceListMap.size()
                     <<"\n"<< printSGServiceMap( "[DIFF SERVICE LIST]", diffHttpServiceListMap));
            LOG_INFO("[DyupsDataPullTimer] after combine, diffProperties.size="<< diffHttpPropertiesMap.size()
                     <<"\n"<< printPropertiesMap( "[DIFF PROPERTIES]", diffHttpPropertiesMap));
			TengineTriger::instance().dyupsDataChanged( diffHttpServiceListMap, diffHttpPropertiesMap);
        } else {
            LOG_INFO("[DyupsDataPullTimer] after check, NO CHANGE at all.");
        }
        
        //6. 落地配置文件
        if (diffHttpServiceListMap.size()>0 || diffHttpPropertiesMap.size()>0) {
            if (0!=dypusDataToFile()) {
                LOG_ERROR("[DyupsDataPullTimer] call dypusDataToFile failed!!!");
            } else {
                LOG_INFO("[DyupsDataPullTimer] dypusDataToFile SUCCEED!");
            }
        }
        
        sleep(_pulling_interval);
    }
}

//=========================== Service List Issue ===============================

vector<SGService> DyupsDataManager::getServiceListByAppkey (const string& appkey) {
    vector<SGService> ret;
    {
        AutoLock auto_lock( &_service_list_mutex);
        for( int i=0 ; i<_biz_code_vec.size() ; ++i) {
            if (_http_service_list_map[_biz_code_vec.at(i)].find(appkey)
                != _http_service_list_map[_biz_code_vec.at(i)].end()) {
                ret = _http_service_list_map[_biz_code_vec.at(i)][appkey];
                return ret;
            }
        }
    }
    return ret;
}

int validSGService(const SGService& service) {
    if (INADDR_NONE==inet_addr(service.ip.c_str())) {
        return -1;
    }
    if (service.port <=0) {
        return -1;
    }
    if (service.weight <= 0) {
        return -1;
    }
    return 0;
}

void DyupsDataManager::tripInvalidSGService (vector< SGService>& serviceList) {
    vector<SGService>::iterator servIter = serviceList.begin();
    while (servIter!=serviceList.end()) {
        if (0>validSGService(*servIter)) {
            LOG_INFO("[tripInvalidSGService] invalid SGService appkey|ip|port = "
                     << servIter->appkey <<"|"<< servIter->ip <<"|"<< servIter->port);
            servIter = serviceList.erase(servIter);
        } else {
            servIter ++;
        }
    }
}

/*
 * 比较newHttpServiceListMap与_http_service_list_map区别：
 *    1. 若appkey下的serviceList存在diff或由新增appKey，则将新serviceList
 *       存入diffHttpServiceListMap并更新_http_service_list_map
 *    2. 若有删除的appkey，只更新_http_service_list_map，并将Tengine中的ip:port列表置空
 *
 * 若ip:port发生变化，则落地配置文件
 */
void DyupsDataManager::getHttpServiceDiff ( int bizCode,
        const map< string, vector< SGService> >& newServiceListMap,
        map< string, vector< SGService> >& diffServiceListMap) {
    AutoLock auto_lock(&_service_list_mutex);

    //若本地缓存为空
    if (_http_service_list_map[bizCode].empty()) {
        LOG_INFO("[getHttpServiceDiff] local cache empty for bizCode="<<bizCode);
        map< string, vector< SGService> >::const_iterator newIter = newServiceListMap.begin();
        for ( ; newIter != newServiceListMap.end() ; ++newIter) {
            if ( (newIter->second).size() >0) {
                vector<SGService> v1( newIter->second);
                diffServiceListMap[newIter->first] = v1;
            }
            vector<SGService> v2( newIter->second);
            _http_service_list_map[bizCode][newIter->first] = v2;
        }
        LOG_DEBUG ("[getHttpServiceDiff] after set, for bizCode=" << bizCode
                   <<" diffServiceListMap.size=" << diffServiceListMap.size()
                   <<" _http_service_list_map[biz].size="<< _http_service_list_map[bizCode].size() );
        return;
    }
    
    //新map与本地缓存做diff
    map< string, vector< SGService> >::iterator currIter= _http_service_list_map[bizCode].begin();
    map< string, vector< SGService> >::const_iterator newIter= newServiceListMap.begin();
    
    while (currIter != _http_service_list_map[bizCode].end()
           && newIter != newServiceListMap.end()) {

        if (currIter->first == newIter->first) {    //appkey相同
            if (isVectorSGServiceDifference( currIter->second, newIter->second)) {
                //若vector<SGService>不相同
                vector<SGService> v1( newIter->second);
                diffServiceListMap[newIter->first] = v1;
                vector<SGService> v2( newIter->second);
                _http_service_list_map[bizCode][newIter->first].swap(v2);
                LOG_DEBUG("[getHttpServiceDiff] appkey's ServiceList changed, new list is" <<
                          printSGServiceVector( newIter->first, _http_service_list_map[bizCode][newIter->first]));
            }
            newIter ++;
            currIter ++;
        } else if ( currIter->first < newIter->first) {
            //currIter->first 为被删除的appkey
            vector<SGService> v;
            diffServiceListMap[currIter->first] = v; //塞入空list
            LOG_DEBUG("[getHttpServiceDiff] appkey has been deleted, "<<currIter->first<< ".  compared to "<<newIter->first);
            //c++03 erase does not returns the next iterator.
            //This works, will increments the iterator but returns the original value for use by erase
            _http_service_list_map[bizCode].erase( currIter++); //appkey从本地cache删除
        } else {
            //newIter->first 为新增appkey
            if ( (newIter->second).size() >0) {
                vector<SGService> v1( newIter->second);
                diffServiceListMap[newIter->first] = v1;
            }
            vector<SGService> v2( newIter->second);
            _http_service_list_map[bizCode][newIter->first] = v2;
            LOG_DEBUG("[getHttpServiceDiff] appkey new, list is " <<
                      printSGServiceVector( newIter->first, _http_service_list_map[bizCode][newIter->first]) );
            newIter ++;
        } 
    }
    
    while( currIter != _http_service_list_map[bizCode].end()) {
        //currIter->first 为被删除的appkey
        vector<SGService> v;
        diffServiceListMap[currIter->first] = v; //塞入空list
        LOG_DEBUG("[getHttpServiceDiff] appkey has been deleted, "<<currIter->first << ".  compared to "<<newIter->first);
        _http_service_list_map[bizCode].erase( currIter++); //appkey从本地cache删除
    }
    
    while( newIter != newServiceListMap.end()) {
        //newIter->first 为新增appkey
        if ( (newIter->second).size() >0) {
            vector<SGService> v1( newIter->second);
            diffServiceListMap[newIter->first] = v1;
        }
        vector<SGService> v2( newIter->second);
        _http_service_list_map[bizCode][newIter->first] = v2;
        LOG_DEBUG("[getHttpServiceDiff] appkey new, list is " <<
                  printSGServiceVector( newIter->first, _http_service_list_map[bizCode][newIter->first]) );
        newIter ++;
    }
}

string DyupsDataManager::printSGServiceVector (const string& prefix, const vector< SGService>& v) {
    ostringstream os;
    os <<"["<< prefix <<"]  ";
    int count=0;
    for (vector< SGService>::const_iterator it = v.begin(); it != v.end() ; ++it) {
        count++;
        os<<" #"<< count <<"# "<< (*it).ip <<":"<< (*it).port <<"-"<< (*it).role <<"-"<< (*it).status <<"-"<< (*it).weight;
    }
    os <<"\n";
    return os.str();
}

string DyupsDataManager::printSGServiceMap (const string& prefix, const map<string, vector< SGService> >& m) {
    ostringstream os;
    os <<"["<< prefix <<"]  ";
    int count=0;
    map< string, vector< SGService> >::const_iterator iter = m.begin();
    for ( ; iter != m.end() ; ++iter) {
        count++;
        os<<" =="<< count <<"== "<< printSGServiceVector(iter->first, iter->second);
    }
    os <<"\n";
    return os.str();
}

bool sort_by_ip_port(const SGService& ser1, const SGService& ser2) {
    if (ser1.ip != ser2.ip) {
        return ser1.ip > ser2.ip;
    } else {
        return ser1.port > ser2.port;
    }
}

//thrift gen出的.cpp用==比较fweight是否相同，不靠谱
bool isSGServiceEqual(const SGService& ser1, const SGService& ser2) {
    if (!(ser1.ip == ser2.ip))
        return false;
    if (!(ser1.port == ser2.port))
        return false;
    if (!(ser1.weight == ser2.weight))
        return false;
    if (!(ser1.status == ser2.status))
        return false;
    if (!(ser1.role == ser2.role))
        return false;
    if (!(ser1.envir == ser2.envir))
        return false;
    return true;
}

//先通过ip:port排序，再比较SGService是否相同
bool DyupsDataManager::isVectorSGServiceDifference (
        const vector< SGService>& v_a, const vector< SGService>& v_b) {
    bool isDiff = false;
    if (v_a.size()==0 && v_b.size()==0) {   //全为空，相同
        isDiff = false;
    } else if ( v_a.size() != v_b.size()) { //size不同一定不同
        isDiff =true;
    } else {                                //size相同且不为空
        vector<SGService> v1(v_a);
        sort( v1.begin(), v1.end(), sort_by_ip_port);
        vector<SGService> v2(v_b);
        sort( v2.begin(), v2.end(), sort_by_ip_port);
        
        for (int i=0, j=0; i<v1.size() && j<v2.size(); i++,j++) {
            if ( !isSGServiceEqual( v1.at(i), v2.at(j)) ) {
                isDiff = true;
                break;
            }
        }
    }
    return isDiff;
}


//========================== Properties Issue ==================================

HttpProperties DyupsDataManager::getPropertiesByAppkey (const string& appkey) {
    HttpProperties ret;
    AutoLock auto_lock( &_properties_mutex);
    for( int i=0 ; i<_biz_code_vec.size() ; ++i) {
        if (_http_properties_map[_biz_code_vec.at(i)].find(appkey)
            != _http_properties_map[_biz_code_vec.at(i)].end()) {
            ret = _http_properties_map[_biz_code_vec.at(i)][appkey];
            return ret;
        }
    }
    return ret;
}

/*
 * 比较newPropertiesMap与_http_properties_map区别：
 *    1. 若appkey下的serviceList存在diff或有新增appKey，则将新serviceList
 *       存入diffHttpServiceListMap并更新_http_service_list_map
 *    2. 对删除了的appKey不做任何处理
 */
void DyupsDataManager::getHttpPropertiesDiff ( int bizCode,
        const map< string, HttpProperties>& newPropertiesMap,
        map< string, HttpProperties>& diffPropertiesMap) {
    AutoLock auto_lock(&_properties_mutex);
    
    //若本地缓存为空
    if (_http_properties_map[bizCode].empty()) {
        LOG_INFO("[getHttpPropertiesDiff] local cache empty for bizCode="<<bizCode);
        map< string, HttpProperties >::const_iterator newIter = newPropertiesMap.begin();
        for ( ; newIter != newPropertiesMap.end() ; ++newIter) {
            if ( (newIter->second).find(HEALTH_CHECK_KEY) != (newIter->second).end()) {
                HttpProperties v1( newIter->second);
                diffPropertiesMap[newIter->first] = v1;
            }
            HttpProperties v2( newIter->second);
            _http_properties_map[bizCode][newIter->first] = v2;
        }
        LOG_DEBUG ("[getHttpPropertiesDiff] after set, for bizCode=" << bizCode
                   <<" diffPropertiesMap.size=" << diffPropertiesMap.size()
                   <<" _http_properties_map[biz].size="<< _http_properties_map[bizCode].size() );
        return;
    }
    
    //新map与本地缓存做diff
    map< string, HttpProperties>::iterator currIter = _http_properties_map[bizCode].begin();
    map< string, HttpProperties>::const_iterator newIter = newPropertiesMap.begin();
    
    while (currIter != _http_properties_map[bizCode].end()
           && newIter != newPropertiesMap.end()) {
        
        if (currIter->first == newIter->first) {    //appkey相同
            if (isPropertiesDifference( currIter->second, newIter->second)) {
                HttpProperties v1( newIter->second);
                diffPropertiesMap[newIter->first] = v1;
                HttpProperties v2( newIter->second);
                _http_properties_map[bizCode][newIter->first].swap(v2);
                LOG_DEBUG("[getHttpPropertiesDiff] appkey's HttpProperties changed, ");
            }
            newIter ++;
            currIter ++;
        } else if ( currIter->first < newIter->first) {
            //currIter->first 为被删除的appkey
            LOG_DEBUG("[getHttpServiceDiff] appkey has been deleted, "<<currIter->first);
            HttpProperties emptyProp;
            (currIter->second).swap(emptyProp); //appkey从本地cache删除
            currIter ++;
        } else {
            //newIter->first 为新增appkey
            if ( (newIter->second).find(HEALTH_CHECK_KEY) != (newIter->second).end()) {
                HttpProperties v1( newIter->second);
                diffPropertiesMap[newIter->first] = v1;
            }
            HttpProperties v2( newIter->second);
            _http_properties_map[bizCode][newIter->first] = v2;
            LOG_DEBUG("[getHttpServiceDiff] appkey new, "<<newIter->first);
            newIter ++;
        }
    }
    
    while( currIter != _http_properties_map[bizCode].end()) {
        //currIter->first 为被删除的appkey
        LOG_DEBUG("[getHttpServiceDiff] appkey has been deleted, "<<currIter->first);
        HttpProperties emptyProp;
        (currIter->second).swap(emptyProp); //appkey从本地cache删除
        currIter ++;
    }
    
    while( newIter != newPropertiesMap.end()) {
        //newIter->first 为新增appkey
        if ( (newIter->second).find(HEALTH_CHECK_KEY) != (newIter->second).end()) {
            HttpProperties v1( newIter->second);
            diffPropertiesMap[newIter->first] = v1;
        }
        HttpProperties v2( newIter->second);
        _http_properties_map[bizCode][newIter->first] = v2;
        LOG_DEBUG("[getHttpServiceDiff] appkey new, "<<newIter->first);
        newIter ++;
    }
}

string DyupsDataManager::printPropertiesMap(
        const string& prefix, const map<string, HttpProperties>& m) {
    ostringstream os;
    os <<"["<< prefix <<"]  ";
    int count=0;
    map< string, HttpProperties >::const_iterator iter = m.begin();
    for ( ; iter != m.end() ; ++iter) {
        count++;
        os<<" =="<< count <<"== "<< iter->first ;
        HttpProperties::const_iterator healthIter = (iter->second).find(HEALTH_CHECK_KEY);
        if (healthIter != (iter->second).end()) {
            os <<"  "<<healthIter->second;
        } else {
            os <<"  NO HEALTH CHECK";
        }
        os <<"\n";
    }
    return os.str();
}

//只判断健康检查方案
bool DyupsDataManager::isPropertiesDifference (
        const HttpProperties& p_a, const HttpProperties& p_b) {
    bool isDiff = false;
    HttpProperties::const_iterator a_iter = p_a.find(HEALTH_CHECK_KEY);
    HttpProperties::const_iterator b_iter = p_b.find(HEALTH_CHECK_KEY);
    
    if (a_iter==p_a.end() && b_iter==p_b.end()) {
        isDiff = false;
    } else if ( a_iter!=p_a.end() && b_iter!=p_b.end()) {
        //都有健康检查，比较策略是否发生了变化
        if (a_iter->second == b_iter->second) {
            isDiff = false;
        } else {
            isDiff = true;
        }
    } else {
        isDiff = true;
    }

    return isDiff;
}


//====================== Data to File ==========================================

string DyupsDataManager::getUpstreamString( const string& appkey,
            const vector<SGService>& httpServiceList,
            const HttpProperties& prop) {
    string upstream = "";
    vector<SGService> revisedServiceList(httpServiceList);
    tripInvalidSGService(revisedServiceList);
    if (revisedServiceList.size() <= 0) {
        return upstream;
    }
    
    HttpProperties::const_iterator propIter = prop.find(HEALTH_CHECK_KEY);
    if (propIter != prop.end()) {
        upstream += propIter->second;
        upstream += "\n\n";
    }
    
    for (int i=0; i<revisedServiceList.size(); ++i) {
        string strategy = "";
        if (0==revisedServiceList.at(i).status || 4==revisedServiceList.at(i).status) {
            strategy = "down";
        } else if (1==revisedServiceList.at(i).role) {
            strategy = "backup";
        }
        try {
            string service = "\t server  ";
            service += revisedServiceList.at(i).ip.c_str();
            service += ":";
            service += boost::lexical_cast<string>(revisedServiceList.at(i).port) ;
            service += "  weight=";
            service += boost::lexical_cast<string>(revisedServiceList.at(i).weight);
            service += " ";
            service += strategy;
            service += " ;\n";
            
            upstream += service;
        } catch (std::exception& e) {
            LOG_ERROR("[getUpstreamString] lexical_cast failed. "<<e.what());
        }

    }
    
    string ret = "upstream ";
    ret += appkey;
    ret += "\n{\n";
    ret += upstream;
    ret += "}\n";
    LOG_DEBUG("[getUpstreamString] "<< ret);
    return ret;
}

int DyupsDataManager::dypusDataToFile() {
    AutoLock auto_lock1( &_service_list_mutex);
    AutoLock auto_lock2( &_properties_mutex);
    
    string nginxConfigPrefix = HlbConfig::instance().m_nginxConfigPrefix;
    string nginxAppkeyConf = HlbConfig::instance().m_nginxAppkeyConf;
    std::string appkeyConfFullPath = nginxConfigPrefix+"/"+ nginxAppkeyConf;
    std::string tmp_appkeyConfFullPath = appkeyConfFullPath + ".tmp";

    FILE * fp = NULL;
    if(NULL == (fp = fopen(tmp_appkeyConfFullPath.c_str(), "w"))) {
        LOG_ERROR("[dypusDataToFile] appkey conf file open error. path= "<<tmp_appkeyConfFullPath);
        return -1;
    }
    
    for (int i=0; i<_biz_code_vec.size(); ++i) {
        int bizCode = _biz_code_vec.at(i);
        map<string, vector<SGService> >::const_iterator servIter = _http_service_list_map[bizCode].begin();
        for ( ; servIter!=_http_service_list_map[bizCode].end(); ++servIter) {
            if ( (servIter->second).size() <= 0) {
                continue;
            } else {
                string appkey = servIter->first;
                HttpProperties props;
                if (_http_properties_map[bizCode].find(appkey) != _http_properties_map[bizCode].end()) {
                    props = _http_properties_map[bizCode][appkey];
                }
                
                fprintf(fp, "%s\n", getUpstreamString( appkey, servIter->second, props).c_str());
            }
        }
    }
    fclose(fp);
    
    if(0 == rename(tmp_appkeyConfFullPath.c_str(), appkeyConfFullPath.c_str())) {
        LOG_INFO("[dypusDataToFile] rename file from " << tmp_appkeyConfFullPath
                 << " to " << appkeyConfFullPath << " success!");
    } else {
        LOG_ERROR("[dypusDataToFile] rename file from " << tmp_appkeyConfFullPath
                 << " to " << appkeyConfFullPath << " fail!");
        return -1;
    }
    return 0;
}


