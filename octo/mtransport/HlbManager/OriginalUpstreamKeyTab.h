//
//  OriginalUpstreamKeyTab.h
//  HlbManager
//
//  Created by zhangjinlu on 15/11/22.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#ifndef __HLB_ORIGINAL_UPSTREAM_KEY_TABLE_H__
#define __HLB_ORIGINAL_UPSTREAM_KEY_TABLE_H__

#include <string>

namespace inf {
namespace hlb {
using namespace std;
//{
//    "upstream_name": "waimai_orderadmin",
//    "is_grey" : 1,
//    "idc" : "shared",
//    "schedule_strategy": {"consistent_hash": "$cookie_SID"},
//    "server": [
//               {   "ip" : "10.64.20.224",
//                   "port" : 8088,
//                   "fail_timeout": 30,
//                   "max_fails": 2,
//                   "slow_start": 100,
//                   "weight": 100
//               }
//               ],
//    "check_strategy": {
//        "check": {
//            "fall": 3,
//            "interval": 3000,
//            "rise": 2,
//            "timeout": 2000,
//            "type": http
//        },
//        "check_http_expect_alive": "http_2xx http_3xx",
//        "check_keepalive_requests" : 100,
//        "check_http_send": "\"GET /test/status HTTP/1.0\\r\\n\\r\\n\""
//    }
//}

typedef struct _upstream_content {
    string upstream_name;
    string schedule_strategy;
    string check_strategy;
    string server_list;
} UpstreamContent;
    
typedef struct _OriginalUpstreamKeyTab {
    _OriginalUpstreamKeyTab() {
        KEY_upstream_name = "upstream_name";
        KEY_is_grey = "is_grey";
        KEY_idc = "idc";
        
        KEY_schedule_strategy = "schedule_strategy";
        
        KEY_server = "server";
        KEY_server_ip = "ip";
        KEY_server_port = "port";
        KEY_server_fail_timeout = "fail_timeout";
        KEY_server_max_fails = "max_fails";
        KEY_server_slow_start = "slow_start";
        KEY_server_weight = "weight";
        
        KEY_check_strategy = "check_strategy";
        KEY_strategy_check = "check";
        KEY_strategy_check_fall = "fall";
        KEY_strategy_check_interval = "interval";
        KEY_strategy_check_rise = "rise";
        KEY_strategy_check_timeout = "timeout";
        KEY_strategy_check_type = "type";
        KEY_strategy_check_http_expect_alive = "check_http_expect_alive";
        KEY_strategy_check_keepalive_requests = "check_keepalive_requests";
        KEY_strategy_check_http_send = "check_http_send";
    }

    string KEY_upstream_name;
    string KEY_is_grey;
    string KEY_idc;
    
    string KEY_schedule_strategy;
    
    string KEY_server;
    string KEY_server_ip;
    string KEY_server_port;
    string KEY_server_fail_timeout;
    string KEY_server_max_fails;
    string KEY_server_slow_start;
    string KEY_server_weight;
    
    string KEY_check_strategy;
    string KEY_strategy_check;
    string KEY_strategy_check_fall;
    string KEY_strategy_check_interval;
    string KEY_strategy_check_rise;
    string KEY_strategy_check_timeout;
    string KEY_strategy_check_type;
    string KEY_strategy_check_http_expect_alive;
    string KEY_strategy_check_keepalive_requests;
    string KEY_strategy_check_http_send;
    
} OriginalUpstreamKeyTab;

}
}
#endif //__HLB_ORIGINAL_UPSTREAM_KEY_TABLE_H__
