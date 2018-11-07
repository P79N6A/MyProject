#ifndef __MAFKA_LOG_H__
#define __MAFKA_LOG_H__

#include "Logger.h"
#include <muduo/base/AsyncLogging.h>
#include <muduo/base/Logging.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

using namespace muduo;
using namespace muduo::net;


namespace mafka
{

extern muduo::AsyncLogging* g_asyncLog;
extern Logger g_Logger;

void SetupLogger(std::string const& filename, std::string const& level = "ERROR", int file_size = 104857600, int file_num = 10);

void SetLogFileName(std::string const& filename);
void SetLogLevel(std::string const& level);
void SetLogFileSize(int file_size);
void SetLogFileNum(int file_num);



#define DEBUG(...) mafka::g_Logger.LogMessage(MAFKA_LOG_LEVEL_DEBUG,__FILE__,__LINE__,__FUNCTION__, __VA_ARGS__)
#define INFO(...) mafka::g_Logger.LogMessage(MAFKA_LOG_LEVEL_INFO,__FILE__,__LINE__,__FUNCTION__, __VA_ARGS__)
#define WARN(...) mafka::g_Logger.LogMessage(MAFKA_LOG_LEVEL_WARN,__FILE__,__LINE__,__FUNCTION__, __VA_ARGS__)
#define ERROR(...) mafka::g_Logger.LogMessage(MAFKA_LOG_LEVEL_ERROR,__FILE__,__LINE__,__FUNCTION__, __VA_ARGS__)

}

#endif //__MAFKA_LOG_H__
