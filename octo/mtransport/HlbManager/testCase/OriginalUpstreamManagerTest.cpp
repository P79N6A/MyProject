//
//  OriginalUpstreamManagerTest.cpp
//  HlbManager
//
//  Created by zhangjinlu on 15/11/20.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#include <stdio.h>
#include <iostream>
#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>

#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>

#include "../hlb_gen_cpp/SGAgent.h"
#include "../utils/log4cplus.h"
#include "../Config.h"
#include "../SgAgentClient.h"
#include "../OriginalUpstreamManager.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace inf::hlb;

std::string jsonStr = "{\n    \"upstream_name\": \"waimaiadmin\",\n    \"is_grey\":1\n    \"idc\" : \"shared\"\n    \"schedule_strategy\": {\"consistent_hash\": \"$cookie_SID\"},\n    \"server\": [\n        {   \"ip\" : \"10.64.20.224\",\n            \"port\" : \"8088\",\n            \"fail_timeout\": \"30\",\n            \"max_fails\": \"2\",\n            \"slow_start\": \"100\",\n            \"weight\": \"100\"\n        },\n        {   \"ip\" : \"10.64.20.211\",\n            \"port\" : \"8088\",\n            \"fail_timeout\": \"30\",\n            \"max_fails\": \"2\",\n            \"slow_start\": \"100\",\n            \"weight\": \"100\"\n        }\n    ],\n    \"check_strategy\": {\n        \"check\": {\n            \"fall\": \"3\",\n            \"interval\": \"3000\",\n            \"rise\": \"2\",\n            \"timeout\": \"2000\",\n            \"type\": \"http\"\n        },\n        \"check_http_expect_alive\": \"http_2xx http_3xx\",\n        \"check_http_send\": \"\\\"GET /test/status HTTP/1.0\\\\r\\\\n\\\\r\\\\n\\\"\"\n    }\n}";

void *OriginalUpstreamUpdater(void * args) {
    OriginalUpstreamManager::instance().OriginalUpstreamPullTimer();
}

void  testIsUpstreamJsonStrValid() {
    bool isValid = 
    OriginalUpstreamManager::instance().isUpstreamJsonStrValid("waimaiadmin", jsonStr);
    cout<<"===== IsUpstreamJsonStrValid =====\n NewStr =  "<<jsonStr<<"\n"<<isValid<<endl;
}

int main(int argc, char** argv) {
    log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT("log4cplus.conf"));
    int line = 0;
    if(0 != (line = HlbConfig::instance().initialization("Hlb.xml"))) {
        cout<< "HlbConfig Init error, at:" << line << " line!";
        return -1;
    }
    cout<<"\n[appkey] "<< HlbConfig::instance().m_hlbManagerAppkey  <<"\n";
    if(! OriginalUpstreamManager::instance().initialization() ) {
        cout<< "OriginalUpstreamManager initialization ERROR\n";
        return -1;
    }
    
    pthread_t updatetid;
    pthread_create( &updatetid, NULL, OriginalUpstreamUpdater, NULL);
    
    testIsUpstreamJsonStrValid();
    //testIsVectorSGServiceDifference();
    
    pthread_join(updatetid, NULL);
    return 0;
}

