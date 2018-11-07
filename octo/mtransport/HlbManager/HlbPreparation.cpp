//
//  HlbPreparation.cpp
//  HlbManager
//
//  Created by zhangjinlu on 16/3/9.
//  Copyright (c) 2016年 zhangjinlu. All rights reserved.
//

#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
#include "Config.h"
#include "./utils/log4cplus.h"
#include "HlbPreparation.h"

using namespace std;
using namespace inf::hlb;

bool HlbPreparation::initialization() {
    bool result = initFiles();
    if (!result) {
        return result;
    }
    return true;
}

//传入变动的HttpServiceList和HttpProperties，触发dyups更新
bool HlbPreparation::initFiles() {
    
    //nginx 配置路径prefix，由puppet管理
    string nginxConfigPrefix = HlbConfig::instance().m_nginxConfigPrefix;
    //HlbManager生成的appkey.conf所在相对路径
    string nginxAppkeyConf = HlbConfig::instance().m_nginxAppkeyConf;
    //迁移upstream落地相对路径
    string nginxOriginalUpstreamPath = HlbConfig::instance().m_nginxOriginalUpstreamPath;
    
    //创建nginxAppkeyConf文件
    string appkeyConfPath = nginxConfigPrefix+"/"+ nginxAppkeyConf;
    FILE * fp = NULL;
    if(NULL == (fp = fopen(appkeyConfPath.c_str(), "w"))) {
        LOG_ERROR("[HlbPreparation] fopen error. path= "<<appkeyConfPath);
        return false;
    }
    fclose(fp);
    
    //创建nginxOriginalUpstreamPath 文件夹
    string dyupsDir = nginxConfigPrefix + nginxOriginalUpstreamPath;
    int md = mkdir( dyupsDir.c_str(), S_IRWXU|S_IRWXG|S_IROTH|S_IXOTH); ;
    if(0>md && EEXIST!=errno) {
        LOG_ERROR("[HlbPreparation] mkdir error. path= "<<dyupsDir<<" errno="<<errno);
        return false;
    }

    return true;
}
