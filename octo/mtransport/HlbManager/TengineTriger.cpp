//
//  TengineTriger.cpp
//  HbManager
//
//  Created by zhangjinlu on 15/11/10.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>
#include <cstring>
#include <sys/types.h>
#include <signal.h>
#include <iostream>
#include <vector>
#include <string>
#include <openssl/md5.h>
#include <jsoncpp/json/json.h>
#include "./utils/log4cplus.h"
#include "Config.h"
#include "HttpClient.h"
#include "TengineTriger.h"
#include "./utils/CommonTool.h"

using namespace std;
using namespace inf::hlb;

static const int MAX_BUFFER_SIZE = 500*1024; //500K, postData最大长度
static const int MAX_CURL_RETRY = 3; //curl最大重试次数
static const int MAX_CURL_RETRY_SLEEPTIME = 5000; //重试睡眠时间 5ms

typedef MD5_CTX  ngx_md5_t;
#define ngx_md5_init    MD5_Init
#define ngx_md5_update  MD5_Update
#define ngx_md5_final   MD5_Final

//传入变动的HttpServiceList和HttpProperties，触发dyups更新
void TengineTriger::dyupsDataChanged(
        map< string, vector< SGService> > diffHttpServiceListMap,
        map< string, HttpProperties> diffPropertiesMap) {
    map< string, vector< SGService> >::iterator servIter = diffHttpServiceListMap.begin();
    for (; servIter!=diffHttpServiceListMap.end() ; ++servIter) {
        string appkey = servIter->first;
        HttpProperties prop;
        if ((servIter->second).size()>0) {
            map< string, HttpProperties>::iterator propIter = diffPropertiesMap.find(appkey);
            if (diffPropertiesMap.find(appkey) != diffPropertiesMap.end()) {
                prop = propIter->second;
            }
        }
        UpdateOctoUpstream( appkey, servIter->second, prop);
    }
}

int TengineTriger::UpdateOctoUpstream( const string& appkey,
                                   const vector<SGService>& httpServiceList,
                                   const HttpProperties& properties) {
    
    char postUrl[512] = {0};
    char postData[MAX_BUFFER_SIZE] = {0};
    char md5HexBuf[128] = {0};
    GenerateMd5(appkey.c_str(), md5HexBuf, sizeof(md5HexBuf));
    
    //含有MD5签名的post请求url
    snprintf (postUrl, sizeof(postUrl), "%s:%d/upstream/%s/%s",
              HlbConfig::instance().m_nginxIp.c_str(),
              HlbConfig::instance().m_nginxDyPort,
              appkey.c_str(),
              md5HexBuf);
    //拼PostData
    getPostDataByServiceList( appkey, httpServiceList, properties, postData, MAX_BUFFER_SIZE);
    
    LOG_INFO("[UpdateUpstream] postUrl:" << postUrl << " postData:" << postData);
    std::string getResponse;
    int code = 0;
    int nRetry = 0;
    while(nRetry < MAX_CURL_RETRY) {
        code = CHttpClient::getInstance()->Post(postUrl, postData, getResponse);
        if (0 == code) {
            break;
        } else {
            LOG_WARN("[UpdateUpstream] fail count="<< nRetry
                     <<" code="<< code <<" "<< getResponse);
        }
        usleep(MAX_CURL_RETRY_SLEEPTIME);
        nRetry++;
    }
    if (nRetry == MAX_CURL_RETRY) {
        LOG_WARN("[UpdateOctoUpstream] fail for appkey="<< appkey<<" code="<< code
                 <<" postUrl:" << postUrl << " postData:" << postData
                 <<"  response="<< getResponse);
    }
    return code;
}

void TengineTriger::getPostDataByServiceList(
            const string& appKeyName,
            const vector<SGService>& httpServiceList,
            const HttpProperties& prop,
            char *postData, int MaxLen) {
    char * p = postData;
    int len = 0;
    
    //灌入健康检查方案
    HttpProperties::const_iterator propIter = prop.find("health_check");
    if (propIter != prop.end()) {
        string health_check = propIter->second;
        len = snprintf(p, postData + MaxLen - p, "%s", health_check.c_str());
        p += len;
    }
    
    //灌入service
    for(size_t i = 0; i < httpServiceList.size(); i++) {
        string strategy = "";
        if (0==httpServiceList[i].status || 4==httpServiceList[i].status) {
            strategy = "down";
        } else if (1==httpServiceList[i].role) {
            strategy = "backup";
        }
        len = snprintf(p, postData + MaxLen - p,
                       "  server %s:%d weight=%d %s; ",
                       httpServiceList[i].ip.c_str(),
                       httpServiceList[i].port,
                       httpServiceList[i].weight,
                       strategy.c_str());
        p += len;
    }
    return;
}

//========================================================================

//传入变动的upstream，触发dyups更新
void TengineTriger::OriginalUpstreamChanged(
            const map<string, UpstreamContent>& diffUpstreamMap) {
    map< string, UpstreamContent >::const_iterator upIter = diffUpstreamMap.begin();
    for (; upIter!=diffUpstreamMap.end() ; ++upIter) {
        if (0!=UpdateOriginalUpstream(upIter->first, upIter->second)) {
            LOG_ERROR("[OriginalUpstreamChanged] Tengine post "<<upIter->first
                      <<" FAILED. Please check "<< (upIter->second).check_strategy
                      <<"  "<< (upIter->second).schedule_strategy
                      <<"  "<< (upIter->second).server_list);
        }
    }
}

int TengineTriger::UpdateOriginalUpstream(
        const string& upstreamName, const UpstreamContent& upstreamContent){
    char postUrl[512] = {0};
    char postData[MAX_BUFFER_SIZE] = {0};
    char md5HexBuf[128] = {0};
    GenerateMd5(upstreamName.c_str(), md5HexBuf, sizeof(md5HexBuf));
    
    //含有MD5签名的post请求url
    snprintf (postUrl, sizeof(postUrl), "%s:%d/upstream/%s/%s",
              HlbConfig::instance().m_nginxIp.c_str(),
              HlbConfig::instance().m_nginxDyPort,
              upstreamName.c_str(),
              md5HexBuf);
    //拼PostData
    getPostDataByUpstreamContent( upstreamName, upstreamContent, postData, MAX_BUFFER_SIZE);
    
    LOG_INFO("[UpdateOriginalUpstream] postUrl:" << postUrl << " postData:" << postData);
    std::string getResponse;
    int code = 0;
    int nRetry = 0;
    while(nRetry < MAX_CURL_RETRY) {
        code = CHttpClient::getInstance()->Post(postUrl, postData, getResponse);
        if (0 == code) {
            break;
        } else {
            LOG_WARN("[UpdateOriginalUpstream] fail for "<< upstreamName
                     <<". count="<< nRetry <<" code="<< code <<" "<< getResponse);
        }
        usleep(MAX_CURL_RETRY_SLEEPTIME);
        nRetry++;
    }
    if (nRetry==MAX_CURL_RETRY) {
        LOG_ERROR("[UpdateOriginalUpstream] fail for "<< upstreamName
                  <<". count="<< nRetry <<" code="<< code <<" "<< getResponse);
    }
    
    return code;
}

void TengineTriger::getPostDataByUpstreamContent(
        const string& upstreamName, const UpstreamContent& upstreamContent,
        char *postData, int MaxLen) {
    char * p = postData;
    int len = 0;
    
    if (upstreamContent.schedule_strategy.find("consistent_hash") == string::npos) {
        len = snprintf(p, postData + MaxLen - p,
                       " %s ", upstreamContent.schedule_strategy.c_str());
        p += len;
    }
    
    len = snprintf(p, postData + MaxLen - p,
                   " %s ", upstreamContent.check_strategy.c_str());
    p += len;
    
    len = snprintf(p, postData + MaxLen - p,
                   " %s ", upstreamContent.server_list.c_str());
    p += len;
    
}

//根据json串内容拼upstream post data
void TengineTriger::getPostDataByUpstreamJsonStr(
        const string& upstreamName, const string& upstreamJsonStr,
        char *postData, int MaxLen) {
    char * p = postData;
    int len = 0;
    
    try {
        Json::Reader reader;
        Json::Value up_root;
        if (reader.parse(upstreamJsonStr, up_root) && up_root.isObject()) {
            //1. 负载均衡策略字段 如：
            //   consistent_hash $cookie_SID;
            if (up_root.isMember(_key_tab.KEY_schedule_strategy)
                && up_root[_key_tab.KEY_schedule_strategy].isObject()) {
                Json::Value::iterator schedule_iter =
                                      up_root[_key_tab.KEY_schedule_strategy].begin();
                if (schedule_iter != up_root[_key_tab.KEY_schedule_strategy].end()) {
                    if ((*schedule_iter).isString() && schedule_iter.memberName()!="consistent_hash") {
                        len = snprintf(p, postData + MaxLen - p,
                                       " %s %s; ",
                                       schedule_iter.memberName(),
                                       (*schedule_iter).asString().c_str());
                        p += len;
                    } else {
                        LOG_WARN("[getPostDataByUpstreamJsonStr] convert wrong. "
                                 <<schedule_iter.memberName() <<" : "<<*schedule_iter);
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
                if (check_iter !=
                        up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].end()) {
                    //在“check”字段内有值时才会生成check关键字及其后续内容
                    len = snprintf(p, postData + MaxLen - p, " check ");
                    p += len;
                    
                    for ( ; check_iter != up_root[_key_tab.KEY_check_strategy][_key_tab.KEY_strategy_check].end(); ++check_iter) {
                        if ((*check_iter).isIntegral()) {
                            len = snprintf(p, postData + MaxLen - p,
                                           " %s=%d ",
                                           check_iter.memberName(),
                                           (*check_iter).asInt());
                            p += len;
                        } else if ((*check_iter).isConvertibleTo(Json::stringValue)) {
                            len = snprintf(p, postData + MaxLen - p,
                                           " %s=%s ",
                                           check_iter.memberName(),
                                           (*check_iter).asString().c_str());
                            p += len;
                        } else {
                            LOG_WARN("[getPostDataByUpstreamJsonStr] convert wrong. "
                                     <<check_iter.memberName() <<" : "<<*check_iter);
                        }
                    }
                    len = snprintf(p, postData + MaxLen - p, " ; ");
                    p += len;
                    
                    if (up_root[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check_http_send)) {
                        len = snprintf(p, postData + MaxLen - p,
                                       " %s %s; ",
                                       _key_tab.KEY_strategy_check_http_send.c_str(),
                                       up_root[_key_tab.KEY_check_strategy].get(_key_tab.KEY_strategy_check_http_send,"").asString().c_str());
                        p += len;
                    }
                    
                    if (up_root[_key_tab.KEY_check_strategy].isMember(_key_tab.KEY_strategy_check_http_expect_alive)) {
                        len = snprintf(p, postData + MaxLen - p,
                                       " %s %s; ",
                                       _key_tab.KEY_strategy_check_http_expect_alive.c_str(),
                                       up_root[_key_tab.KEY_check_strategy].get(_key_tab.KEY_strategy_check_http_expect_alive,"").asString().c_str());
                        p += len;
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
                        len = snprintf(p, postData + MaxLen - p,
                                       " server %s:%d ",
                                       up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_ip].asString().c_str(),
                                       up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_port].asInt());
                        p += len;
                        
                        if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_fail_timeout)) {
                            len = snprintf(p, postData + MaxLen - p,
                                           " %s=%d ",
                                           _key_tab.KEY_server_fail_timeout.c_str(),
                                           up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_fail_timeout].asInt());
                            p += len;
                        }
                        
                        if (up_root[_key_tab.KEY_server][i].isMember(_key_tab.KEY_server_max_fails)) {
                            len = snprintf(p, postData + MaxLen - p,
                                           " %s=%d ",
                                           _key_tab.KEY_server_max_fails.c_str(),
                                           up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_max_fails].asInt());
                            p += len;
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
                            len = snprintf(p, postData + MaxLen - p,
                                           " %s=%d ",
                                           _key_tab.KEY_server_weight.c_str(),
                                           up_root[_key_tab.KEY_server][i][_key_tab.KEY_server_weight].asInt());
                            p += len;
                        }
                        
                        len = snprintf(p, postData + MaxLen - p, " ; ");
                        p += len;
                    }
                }
            }
        } else {
                std::cout  << "[getPostDataByUpstreamJsonStr] parse upstream FAILED. "
                << reader.getFormattedErrorMessages();
        }
    } catch (std::exception& e) {
        LOG_ERROR("[getPostDataByUpstreamJsonStr] parse upstream ERROR. "<<e.what());
    }

    return;
}


void TengineTriger::GenerateMd5(const char *upstreamName, char *md5HexBuf, size_t bufLength)
{
    char buf[1024] = {0};
    snprintf(buf, sizeof(buf), "%s\t%s", upstreamName, HlbConfig::instance().m_nginxSecureKey.c_str());
    unsigned char md5Buf[16] = {0};
    ngx_md5_t md5;
    ngx_md5_init(&md5);
    ngx_md5_update(&md5, buf, strlen(buf));
    ngx_md5_final(md5Buf, &md5);
    
    char *p = md5HexBuf;
    size_t i =0;
    for(; i < sizeof(md5Buf); i++) {
        p += snprintf(p, p+bufLength - md5HexBuf, "%02x", md5Buf[i]);
    }
    return;
}

//只负责触发Tengine执行 “nginx -s reload” 或 “-s start”
int TengineTriger::reloadTengine() {
    
    struct stat buf = {0};
    std::string pidFullPath = HlbConfig::instance().m_nginxPidPath;
    if(-1 == stat(pidFullPath.c_str(), &buf) || 0 == buf.st_size) {
        LOG_INFO("[reloadTengine] PID file exists, gonna call -s start. "<<pidFullPath.c_str());
        std::string startFullCommand = HlbConfig::instance().m_nginxBinPath + " -s start";
        if(-1 == system(startFullCommand.c_str())) {
            LOG_ERROR("[reloadTengine] " << startFullCommand << " 命令执行失败!!!");
            return -1;
        } else {
            LOG_INFO("[reloadTengine] "<< startFullCommand <<" 命令执行完成");
        }
    } else {
        LOG_INFO("[reloadTengine] PID file exists, gonna call -s reload. "<<pidFullPath.c_str());
        std::string reloadFullCommand = HlbConfig::instance().m_nginxBinPath + " -s reload";
        if(-1 == system(reloadFullCommand.c_str())) {
            LOG_ERROR("[reloadTengine] " << reloadFullCommand << " 命令执行失败!!!");
            return -1;
        } else {
            LOG_INFO("[reloadTengine] "<< reloadFullCommand <<" 命令执行完成");
        }
    }

    return 0;
}

