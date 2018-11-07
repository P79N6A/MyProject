//
//  OriginalUpstreamManager.cpp
//  HlbManager
//
//  Created by zhangjinlu on 15/11/19.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#include <iostream>
#include <vector>
#include <string>
#include <arpa/inet.h>
#include <boost/lexical_cast.hpp>
#include <jsoncpp/json/json.h>
#include "./utils/log4cplus.h"
#include "Config.h"
#include "TengineTriger.h"
#include "OriginalUpstreamManager.h"

using namespace std;
using namespace inf::hlb;

bool OriginalUpstreamManager::initialization() {
    
    //初始化MNSC client
    _mnsc_client_controller = boost::shared_ptr<MNSCacheClientCollector> (
            new MNSCacheClientCollector());
    
    //初始化MNSC轮询时间间隔
    int pullingInterval = HlbConfig::instance().m_mnscUpdateTime;
    if (pullingInterval>0) {
        _pulling_interval = pullingInterval;
    }
    //本节点是否属于nginx灰度分组
    _is_grey = HlbConfig::instance().m_upstreamIsGrey;
    //upstream节点相关参数
    _idc_type = HlbConfig::instance().m_upstreamIDCType;
    _nginx_type = HlbConfig::instance().m_upstreamNginxType;
    
    if (_idc_type.empty() || _nginx_type.empty()) {
        LOG_ERROR("[initialization] empry config, _idc_type="<<_idc_type
                   <<" _nginx_type="<<_nginx_type <<" _is_grey="<<_is_grey);
    }
    LOG_INFO("[initialization] _idc_type:"<<_idc_type
             <<" _nginx_type:"<<_nginx_type <<" _is_grey:"<<_is_grey );
    
    return true;
}

void OriginalUpstreamManager::OriginalUpstreamPullTimer() {
    LOG_INFO( "[OriginalUpstreamPullTimer] start. _pulling_interval = "<< _pulling_interval);
    map< string, string > diffUpstreamMap;
    
    //_json_upstream_map;
    while(true) {
        diffUpstreamMap.clear();
        
        //1. 拉取对应环境下的upstream map
        map< string, string> newUpstreamMap;
        _mnsc_client_controller->getHlbUpstream(newUpstreamMap, _nginx_type, _idc_type);
        
        //2. 比较新upstream map与当前区别
        if (newUpstreamMap.size() >0) {
            getUpstreamDiff( newUpstreamMap, diffUpstreamMap);
        }
        
        //3. 将diffUpstreamMap json转成UpstreamContent表示
        map<string, UpstreamContent> diffUpstreamContentMap;
        getUpstreamContentMap( diffUpstreamMap, diffUpstreamContentMap);
        
        //4. 触发Tengine更新
        if (diffUpstreamContentMap.size()>0) {
            LOG_INFO("[OriginalUpstreamPullTimer] diff upstream.size= "<<diffUpstreamContentMap.size()
                     <<". Gonna triger Tengine update.");
            TengineTriger::instance().OriginalUpstreamChanged( diffUpstreamContentMap);
        } else {
            LOG_INFO("[OriginalUpstreamPullTimer] after check, NO CHANGE at all.");
        }
        
        //5. 若有diff则落地配置文件
        if (diffUpstreamContentMap.size()>0 ) {
            map<string, UpstreamContent>::const_iterator iter = diffUpstreamContentMap.begin();
            for ( ; iter!=diffUpstreamContentMap.end(); ++iter) {
                if (0!=OriginalUpstreamToFile( iter->first, iter->second)) {
                    LOG_ERROR("[OriginalUpstreamPullTimer] call OriginalUpstreamToFile FAILED!!! "<<iter->first);
                } else {
                    LOG_INFO("[OriginalUpstreamPullTimer] OriginalUpstreamToFile SUCCEED for "<<iter->first);
                }
            }
        }
        sleep(_pulling_interval);
    }
}

/*
 * 遍历newUpstreamMap：
 *    1. 读取is_grey字段，若该upstream不需更新则忽略之
 *    2. 判断upstream合法性，若不合法曾忽略之
 *    3. 比较upstream与本地缓存_json_upstream_map区别，若有diff则更新本地缓存，并存入diffUpstreamMap
 */
void OriginalUpstreamManager::getUpstreamDiff(
        const map<string, string>& newUpstreamMap,
        map<string, string>& diffUpstreamMap) {
    AutoLock auto_lock(&_json_upstream_mutex);
    
    //若本地缓存为空
    if (_json_upstream_map.empty()) {
        LOG_INFO("[getUpstreamDiff] local cache empty");
        map< string, string>::const_iterator newIter = newUpstreamMap.begin();
        for ( ; newIter != newUpstreamMap.end() ; ++newIter) {
            if (!enableUpdate( newIter->first, newIter->second)) {
                continue;
            }
            if (!isUpstreamJsonStrValid( newIter->first, newIter->second)) {
                continue;
            }
            _json_upstream_map[newIter->first] = newIter->second;
            diffUpstreamMap[newIter->first] = newIter->second;
        }
        LOG_DEBUG ("[getUpstreamDiff] after set, newUpstreamMap.size=" << newUpstreamMap.size()
                   <<" diffUpstreamMap.size=" <<diffUpstreamMap.size());
        return;
    }
    
    //新map与本地缓存做diff
    map< string, string>::iterator currIter= _json_upstream_map.begin();
    map< string, string>::const_iterator newIter= newUpstreamMap.begin();
    
    while (currIter != _json_upstream_map.end()
           && newIter != newUpstreamMap.end()) {
        if (currIter->first == newIter->first) {    //upstream_name相同
            if (enableUpdate(newIter->first, newIter->second)
                && isUpstreamJsonStrValid(newIter->first, newIter->second)) {
                if (isUpstreamJsonStrDiff(newIter->second, currIter->second)) {
                    diffUpstreamMap[newIter->first] = newIter->second;
                    _json_upstream_map[currIter->first] = newIter->second;
                    LOG_DEBUG("[getUpstreamDiff] upstream: "<< newIter->first
                              <<" changed to: "<<newIter->second);
                }
            }
            newIter ++;
            currIter ++;
        } else if ( currIter->first < newIter->first) {
            //currIter->first 为被删除的upstream
            diffUpstreamMap[currIter->first] = ""; //塞入空string
            LOG_DEBUG("[getUpstreamDiff] upstream has been deleted, "<<currIter->first);
            _json_upstream_map.erase( currIter++); //upstream从本地cache删除
        } else {
            //newIter->first 为新增upstream
            if (enableUpdate(newIter->first, newIter->second)
                && isUpstreamJsonStrValid(newIter->first, newIter->second)) {
                diffUpstreamMap[newIter->first] = newIter->second;
                _json_upstream_map[newIter->first] = newIter->second;
                LOG_DEBUG("[getUpstreamDiff] new upstream: " <<newIter->first
                          <<"  "<<newIter->second);
            }
            newIter ++;
        }
    }
    
    while( currIter != _json_upstream_map.end()) {
        //currIter->first 为被删除的upstream
        diffUpstreamMap[currIter->first] = ""; //塞入空string
        LOG_DEBUG("[getUpstreamDiff] upstream has been deleted, "<<currIter->first);
        _json_upstream_map.erase( currIter++); //upstream从本地cache删除
    }
    
    while( newIter != newUpstreamMap.end()) {
        //newIter->first 为新增upstream
        if (enableUpdate(newIter->first, newIter->second)
            && isUpstreamJsonStrValid(newIter->first, newIter->second)) {
            diffUpstreamMap[newIter->first] = newIter->second;
            _json_upstream_map[newIter->first] = newIter->second;
            LOG_DEBUG("[getUpstreamDiff] new upstream: " <<newIter->first
                      <<"  "<<newIter->second);
        }
        newIter ++;
    }
}

//根据is_grey字段判断该upstream是否要被更新
bool OriginalUpstreamManager::enableUpdate(
        const string& upstreamName, const string& upstreamJsonStr) {
    bool isGonnaUpdate = false;
    if (1==_is_grey) { //若本HLB节点为灰度节点，均返回true
        isGonnaUpdate = true;
    } else if (0==_is_grey) { //普通HLB节点只更新is_grey=0的upstream
        try {
            Json::Reader reader;
            Json::Value json_root;
            if (reader.parse(upstreamJsonStr, json_root) && json_root.isObject()
                && json_root.isMember(_key_tab.KEY_is_grey)
                && json_root[_key_tab.KEY_is_grey].isIntegral()) {
                if (0==json_root[_key_tab.KEY_is_grey].asInt()) {
                    isGonnaUpdate =true;
                }
                LOG_DEBUG("[enableUpdate] is_grey="<<json_root[_key_tab.KEY_is_grey]
                          <<" for upstream= "<<upstreamName <<" isGonnaUpdate="<<isGonnaUpdate);
            } else {
                LOG_ERROR("[enableUpdate] parse json FAILED for upstream="
                          <<upstreamName <<"jsonStr= "<<upstreamJsonStr);
            }
        } catch (std::exception& e) {
            LOG_ERROR("[enableUpdate] parse json ERROR for "<< upstreamName <<e.what() );
        }
    }
    if (!isGonnaUpdate) {
        LOG_INFO("[enableUpdate] ignore upstream "<<upstreamJsonStr);
    }
    return isGonnaUpdate;
}

//判断json串是否合法
bool OriginalUpstreamManager::isUpstreamJsonStrValid (
        const string& upstreamName, const string& upstreamJsonStr){
    bool isValid = true;
    try {
        Json::Reader reader;
        Json::Value json_root;
        if (reader.parse(upstreamJsonStr, json_root) && json_root.isObject()){
            if (!json_root.isMember(_key_tab.KEY_server)
                || !json_root[_key_tab.KEY_server].isArray()
                || json_root[_key_tab.KEY_server].size() <=0) {

                isValid =false;
            } else {
                for (int i=0; i<json_root[_key_tab.KEY_server].size(); i++) {
                    string ip = json_root[_key_tab.KEY_server][i].get(_key_tab.KEY_server_ip,"").asString();
                    if (INADDR_NONE == inet_addr(ip.c_str())) {
                        isValid =false;
                        break;
                    }
                    if (json_root[_key_tab.KEY_server][i].get(_key_tab.KEY_server_port,-1).asInt() <=0) {
                        isValid =false;
                        break;
                    }
                }
            }
        }
    } catch (std::exception& e) {
        LOG_ERROR("[isUpstreamJsonStrValid] parse json ERROR. "<<e.what() <<" upstream = "<<upstreamJsonStr);
        isValid = false;
    }
    
    if (!isValid) {
        LOG_WARN("[isUpstreamJsonStrValid] INVALID UPSTREAM "<<upstreamJsonStr);
    }
    return isValid;
}

bool OriginalUpstreamManager::isUpstreamJsonStrDiff (
        const string& up_a, const string& up_b) {
    bool isDiff = false;
    if (up_a == up_b) {
        LOG_DEBUG("[isUpstreamJsonStrDiff] SAME_STR. str_a == str_b");
        return false;
    }
    try {
        Json::Reader reader;
        Json::Value root_a;
        Json::Value root_b;
        if (reader.parse(up_a, root_a) && root_a.isObject()
            && reader.parse(up_b, root_b) && root_b.isObject()){
            //比较负载均衡
            if ((!isDiff)
                && root_a.isMember(_key_tab.KEY_schedule_strategy)
                && root_b.isMember(_key_tab.KEY_schedule_strategy)
                && root_a[_key_tab.KEY_schedule_strategy].isObject()
                && root_b[_key_tab.KEY_schedule_strategy].isObject()) {
                Json::Value::iterator a_schedule_iter =
                            root_a[_key_tab.KEY_schedule_strategy].begin();
                Json::Value::iterator b_schedule_iter =
                            root_b[_key_tab.KEY_schedule_strategy].begin();
                if (a_schedule_iter != root_a[_key_tab.KEY_schedule_strategy].end()
                    && b_schedule_iter != root_b[_key_tab.KEY_schedule_strategy].end()
                    && (a_schedule_iter.memberName() == b_schedule_iter.memberName())
                    && ((*a_schedule_iter).asString() == (*b_schedule_iter).asString())) {
                    isDiff = false;
                } else if (a_schedule_iter == root_a[_key_tab.KEY_schedule_strategy].end()
                           && b_schedule_iter == root_b[_key_tab.KEY_schedule_strategy].end()) {
                    isDiff = false;
                } else {
                    isDiff = true;
                }
            } else {
                isDiff = true;
            }
            
            //比较check
            if ((!isDiff)
                && root_a.isMember(_key_tab.KEY_check_strategy)
                && root_b.isMember(_key_tab.KEY_check_strategy)) {
                
                if ((!isDiff) &&
                    (root_a[_key_tab.KEY_check_strategy].
                        get(_key_tab.KEY_strategy_check_http_expect_alive,"").asString()
                     == root_b[_key_tab.KEY_check_strategy].
                        get(_key_tab.KEY_strategy_check_http_expect_alive,"").asString())) {
                    ;
                } else {
                    isDiff = true;
                }
                
                if ((!isDiff) &&
                    (root_a[_key_tab.KEY_check_strategy].
                        get(_key_tab.KEY_strategy_check_http_send,"").asString()
                     == root_b[_key_tab.KEY_check_strategy].
                        get(_key_tab.KEY_strategy_check_http_send,"").asString())) {
                    ;
                } else {
                    isDiff = true;
                }
                
                if ((!isDiff)
                    && root_a[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check)
                    && root_b[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check)
                    && root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].isObject()
                    && root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].isObject()) {

                    if ((!isDiff) &&
                        (root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                            get(_key_tab.KEY_strategy_check_type, "").asString()
                         == root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                            get(_key_tab.KEY_strategy_check_type, "").asString())) {
                        ;
                    } else {
                        isDiff = true;
                    }
                    
                    if ((!isDiff) &&
                        (root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                            get(_key_tab.KEY_strategy_check_timeout, -1).asInt()
                         == root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                            get(_key_tab.KEY_strategy_check_timeout, -1).asInt())) {
                        ;
                    } else {
                        isDiff = true;
                    }
                    
                    if ((!isDiff) &&
                        (root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_interval, -1).asInt()
                         == root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_interval, -1).asInt())) {
                        ;
                    } else {
                        isDiff = true;
                    }
                    
                    if ((!isDiff) &&
                        (root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_rise, -1).asInt()
                         == root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_rise, -1).asInt())) {
                        ;
                    } else {
                        isDiff = true;
                    }
                    
                    if ((!isDiff) &&
                        (root_a[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_fall, -1).asInt()
                         == root_b[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].
                         get(_key_tab.KEY_strategy_check_fall, -1).asInt())) {
                        ;
                    } else {
                        isDiff = true;
                    }
                } else {
                    isDiff = true;
                }
            } else {
                isDiff = true;
            }
            
            
            //最后比较server list，塞入vector排序
            if ((!isDiff) &&
                (root_a[_key_tab.KEY_server].size() == root_b[_key_tab.KEY_server].size()) ) {
                vector<ServerNode> server_list_a;
                for (int i=0; i<root_a[_key_tab.KEY_server].size(); i++) {
                    ServerNode tmp_node;
                    tmp_node._ip = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_ip,"").asString();
                    tmp_node._port = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_port,-1).asInt();
                    tmp_node._fail_timeout = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_fail_timeout,-1).asInt();
                    tmp_node._max_fails = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_max_fails,-1).asInt();
                    tmp_node._slow_start = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_slow_start,-1).asInt();
                    tmp_node._weight = root_a[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_weight,-1).asInt();
                    server_list_a.push_back(tmp_node);
                }
                vector<ServerNode> server_list_b;
                for (int i=0; i<root_b[_key_tab.KEY_server].size(); i++) {
                    ServerNode tmp_node;
                    tmp_node._ip = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_ip,"").asString();
                    tmp_node._port = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_port,-1).asInt();
                    tmp_node._fail_timeout = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_fail_timeout,-1).asInt();
                    tmp_node._max_fails = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_max_fails,-1).asInt();
                    tmp_node._slow_start = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_slow_start,-1).asInt();
                    tmp_node._weight = root_b[_key_tab.KEY_server][i].
                                get(_key_tab.KEY_server_weight,-1).asInt();
                    server_list_b.push_back(tmp_node);
                }
                isDiff = isServerNodeVecDiff( server_list_a, server_list_b);
            } else {
                isDiff = true;
            }
        }
    } catch (std::exception& e) {
        LOG_ERROR("[isUpstreamJsonStrDiff] parse json ERROR. "<<e.what());
    }
    
    return isDiff;
}

bool sort_by_ip_port(const ServerNode& ser1, const ServerNode& ser2) {
    if (ser1._ip != ser2._ip) {
        return ser1._ip > ser2._ip;
    } else {
        return ser1._port > ser2._port;
    }
}

bool isServerNodeEqual(const ServerNode& ser1, const ServerNode& ser2) {
    if (!(ser1._ip == ser2._ip))
        return false;
    if (!(ser1._port == ser2._port))
        return false;
    if (!(ser1._weight == ser2._weight))
        return false;
    if (!(ser1._slow_start == ser2._slow_start))
        return false;
    if (!(ser1._max_fails == ser2._max_fails))
        return false;
    if (!(ser1._fail_timeout == ser2._fail_timeout))
        return false;
    return true;
}

bool OriginalUpstreamManager::isServerNodeVecDiff(
            vector<ServerNode>& v_a, vector<ServerNode>& v_b) {
    bool isDiff = false;
    if (v_a.size()==0 && v_b.size()==0) {   //全为空，相同
        isDiff = false;
    } else if ( v_a.size() != v_b.size()) { //size不同一定不同
        isDiff =true;
    } else {                                //size相同且不为空
        sort( v_a.begin(), v_a.end(), sort_by_ip_port);
        sort( v_b.begin(), v_b.end(), sort_by_ip_port);
        
        for (int i=0, j=0; i<v_a.size() && j<v_b.size(); i++,j++) {
            if ( !isServerNodeEqual( v_a.at(i), v_b.at(j)) ) {
                isDiff = true;
                break;
            }
        }
    }
    return isDiff;
}

void OriginalUpstreamManager::getUpstreamContentMap(
            const map<string, string>& jsonUpstreamMap,
            map<string, UpstreamContent>& contentMap) {
    for (map<string,string>::const_iterator json_iter = jsonUpstreamMap.begin();
         json_iter != jsonUpstreamMap.end(); ++json_iter) {
        UpstreamContent tmpContent;
        try {
            Json::Reader reader;
            Json::Value up_root;
            if (reader.parse(json_iter->second, up_root) && up_root.isObject()) {
                //1. 负载均衡策略字段 如：
                //   consistent_hash $cookie_SID;
                if (up_root.isMember(_key_tab.KEY_schedule_strategy)
                    && up_root[_key_tab.KEY_schedule_strategy].isObject()) {
                    Json::Value::iterator schedule_iter = up_root[_key_tab.KEY_schedule_strategy].begin();
                    if (schedule_iter != up_root[_key_tab.KEY_schedule_strategy].end()) {
                        if ((*schedule_iter).isString() ) {
                            tmpContent.schedule_strategy  = schedule_iter.memberName();
                            tmpContent.schedule_strategy += " ";
                            tmpContent.schedule_strategy += (*schedule_iter).asString();
                            tmpContent.schedule_strategy += "; ";
                        }
                    }
                }
                
                //2. 节点健康检查字段 如：
                //   check interval=3000 rise=2 fall=5 timeout=1000 type=http;
                //   check_keepalive_requests 100;
                //   check_http_send "HEAD / HTTP/1.1\r\nConnection: keep-alive\r\n\r\n";
                //   check_http_expect_alive http_2xx http_3xx;
                if (up_root.isMember(_key_tab.KEY_check_strategy)
                    && up_root[_key_tab.KEY_check_strategy].isObject()
                    && up_root[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check)
                    && up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].isObject()) {
                    Json::Value::iterator check_iter =
                         up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].begin();
                    if (check_iter != up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].end()) {
                        tmpContent.check_strategy = " check ";
                        
                        for ( ; check_iter != up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].end(); ++check_iter) {
                            if ((*check_iter).isIntegral()) {
                                tmpContent.check_strategy += check_iter.memberName();
                                tmpContent.check_strategy += "=";
                                tmpContent.check_strategy += boost::lexical_cast<string>((*check_iter).asInt());
                                tmpContent.check_strategy += " ";
                            } else if ((*check_iter).isConvertibleTo(Json::stringValue)) {
                                tmpContent.check_strategy += check_iter.memberName();
                                tmpContent.check_strategy += "=";
                                tmpContent.check_strategy += (*check_iter).asString();
                                tmpContent.check_strategy += " ";
                            } else {
                                LOG_WARN("[getUpstreamContentMap] convert wrong. "
                                         <<check_iter.memberName() <<" : "<<*check_iter);
                            }
                        }
                        tmpContent.check_strategy += " ; ";
                        
                        if (up_root[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check_http_send)) {
                            tmpContent.check_strategy += _key_tab.KEY_strategy_check_http_send;
                            tmpContent.check_strategy += " ";
                            tmpContent.check_strategy += up_root[_key_tab.KEY_check_strategy].get(_key_tab.KEY_strategy_check_http_send, "").asString();
                            tmpContent.check_strategy += "; ";
                        }
                        
                        if (up_root[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check_http_expect_alive)) {
                            tmpContent.check_strategy += _key_tab.KEY_strategy_check_http_expect_alive;
                            tmpContent.check_strategy += " ";
                            tmpContent.check_strategy += up_root[_key_tab.KEY_check_strategy].get(_key_tab.KEY_strategy_check_http_expect_alive,"").asString();
                            tmpContent.check_strategy += "; ";
                        }
                    }
                }
                
                //3. server节点, 如：
                //   server 127.0.0.1:8080 max_fails=3 fail_timeout=30;
                if (up_root.isMember(_key_tab.KEY_server)
                    && up_root[_key_tab.KEY_server].isArray()
                    && up_root[_key_tab.KEY_server].size()>0){
                    for (int i=0; i<up_root[_key_tab.KEY_server].size(); i++) {
                        if (up_root[_key_tab.KEY_server][i].get(_key_tab.KEY_server_ip, "").asString().size() >0
                            && up_root[_key_tab.KEY_server][i].get(_key_tab.KEY_server_port, -1).asInt() >0) {
                            tmpContent.server_list += " server ";
                            tmpContent.server_list += up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_ip].asString();
                            tmpContent.server_list += ":";
                            tmpContent.server_list += boost::lexical_cast<string>(
                                            up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_port].asInt());
                            tmpContent.server_list += " ";
                            
                            if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_fail_timeout)) {
                                tmpContent.server_list += _key_tab.KEY_server_fail_timeout;
                                tmpContent.server_list += "=";
                                tmpContent.server_list += boost::lexical_cast<string>(
                                            up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_fail_timeout].asInt());
                                tmpContent.server_list += " ";
                            }
                            
                            if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_max_fails)) {
                                tmpContent.server_list += _key_tab.KEY_server_max_fails;
                                tmpContent.server_list += "=";
                                tmpContent.server_list += boost::lexical_cast<string>(
                                            up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_max_fails].asInt());
                                tmpContent.server_list += " ";
                            }
                            
                            if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_slow_start)) {
                                /*  暂不支持slow_start
                                 len = snprintf(p, postData + MaxLen - p,
                                 " %s=%d ",
                                 _key_tab.KEY_server_slow_start.c_str(),
                                 up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_slow_start].asInt());
                                 p += len;
                                 */
                            }
                            
                            if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_weight)) {
                                tmpContent.server_list += _key_tab.KEY_server_weight;
                                tmpContent.server_list += "=";
                                tmpContent.server_list += boost::lexical_cast<string>(
                                            up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_weight].asInt());
                                tmpContent.server_list += " ";
                            }
                            tmpContent.server_list += " ; ";
                        }
                    }
                }
            } else {
                LOG_WARN( "[getUpstreamContentMap] parse upstream FAILED. "
                << reader.getFormattedErrorMessages() );
                continue;
            }
        } catch (std::exception& e) {
            LOG_ERROR("[getUpstreamContentMap] parse upstream ERROR. "<<e.what());
            continue;
        }
        tmpContent.upstream_name = json_iter->first;
        contentMap[json_iter->first] = tmpContent;
    }
}

//====================== Data to File ==========================================
int OriginalUpstreamManager::OriginalUpstreamToFile(
        const string& upstreamName, const UpstreamContent& content) {
    
    string nginxConfigPrefix = HlbConfig::instance().m_nginxConfigPrefix;
    string nginxOriginalUpstreamPath = HlbConfig::instance().m_nginxOriginalUpstreamPath;
    string upstreamPath = nginxConfigPrefix + nginxOriginalUpstreamPath +"/"+ upstreamName +".conf";
    string tmp_upstreamPath = upstreamPath + ".tmp";
    
    FILE * fp = NULL;
    if(NULL == (fp = fopen(tmp_upstreamPath.c_str(), "w"))) {
        LOG_ERROR("[OriginalUpstreamToFile] file open error. path= "<<tmp_upstreamPath);
        return -1;
    }
    
    string up = "upstream ";
    up += upstreamName;
    up += "\n{\n";
    if (!content.schedule_strategy.empty()) {
        up += "\t";
        up += content.schedule_strategy;
        up += "\n";
    }
    if (!content.check_strategy.empty()) {
        up += "\t";
        up += content.check_strategy;
        up += "\n";
    }
    if (!content.server_list.empty()) {
        up += "\t";
        up += content.server_list;
        up += "\n";
    }
    up += "}";

    fprintf(fp, "%s", up.c_str());
    fclose(fp);

    if(0 == rename(tmp_upstreamPath.c_str(), upstreamPath.c_str())) {
        LOG_INFO("[OriginalUpstreamToFile] rename file from " << tmp_upstreamPath
                 << "to " << upstreamPath << " success!");
    } else {
        LOG_ERROR("[OriginalUpstreamToFile] rename file from " << tmp_upstreamPath
                  << "to " << upstreamPath << " fail!");
        return -1;
    }
    return 0;
}

