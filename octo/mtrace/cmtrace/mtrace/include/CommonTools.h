/**
* @file CommonTools.h
* @Brief 定义公共函数
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-14
*/

#ifndef __COMMMONTOOLS_H__
#define __COMMMONTOOLS_H__

#include <string>
using namespace std;

extern int g_level; ///控制是否打印Debug日志，非0就打印
extern const int SgAgentPort; 
extern const bool SUCCESS;
extern const int SLEEPTIME;
extern const int SLOWQUERY_SEND_GAP;
extern const int SLOWQUERY_LIST_SIZE;

enum DEBUG_PARAM
{
    ADD_DEBUG_LOG_FULLSAMPLE = 1, ///<可以看到DEBUG日志，并进行全采样输出
    ADD_DEBUG_LOG = 2  ///<可以看到DEBUG日志，并用线上采样频率进行采样
};

/**
* @Brief 获取主机名
* @return 
*/
string getHostName();

/**
* @Brief 获取机器IP
* @return 
*/
string getMachineIp();

/**
* @Brief 获取当前时间对应的毫秒
* @return 
*/
uint64_t getCurrentMilliTime();

/**
* @Brief 获取当前时间对应的微妙
* @return 
*/
uint64_t getCurrentMicroTime();

/**
* @Brief 获取sgAgentIp 
* @return 
*/
std::string getSgAgentIp();

#define COMM_DEBUG(format_string,...)                               \
{                                                                   \
    if (g_level != 0) {                                             \
        time_t now;                                                 \
        char dbgtime[26] ;                                          \
        time(&now);                                                 \
        ctime_r(&now, dbgtime);                                     \
        dbgtime[24] = '\0';                                         \
        fprintf(stderr,"[%s,%d] [%s] " format_string " \n", __FILE__, __LINE__,dbgtime,##__VA_ARGS__); \
    }                                                               \
}

#endif
