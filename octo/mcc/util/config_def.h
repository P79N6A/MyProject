#ifndef __CONFIG_DEF_H__
#define __CONFIG_DEF_H__

const static int UPDATE_TIME_ONLINE = 2000000; // 轮训间隔时间2s
const static int UPDATE_TIME_OFFLIE = 2000000; // 轮训间隔时间2s

const static int RETRY_TIMES = 3; // 实例init重试次数

const static int SAME_VERSION_CODE = -201108;

const static std::string LOGCONF = "/opt/meituan/apps/sg_agent/log4cplus.conf";

#endif
