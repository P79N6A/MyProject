#ifndef __MAFKA_LOGGER_H__
#define __MAFKA_LOGGER_H__

#include <stdarg.h>
#include <time.h>
#include <stdio.h>
#include <strings.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <deque>
#include <string>
#include <pthread.h>
#include <sys/time.h>
#include "Common.h"


#define MAFKA_LOG_LEVEL_CLOSE 0
#define MAFKA_LOG_LEVEL_ERROR 1
#define MAFKA_LOG_LEVEL_WARN  2
#define MAFKA_LOG_LEVEL_INFO  3
#define MAFKA_LOG_LEVEL_DEBUG 4


namespace mafka
{

using std::deque;
using std::string;
class Mutex;
/** 
* @brief 简单的日志系统 
*/
class Logger {
public:
    Logger();
    ~Logger();
    /** 
     * @brief 
     * 
     * @param filename
     * @param fmt
     */
    void RotateLog(const char *filename, const char *fmt = NULL);
    /** 
     * @brief 将日志内容写入文件
     * 
     * @param level 日志的级别
     * @param file  日志内容所在的文件
     * @param line  日志内容所在的文件的行号
     * @param function 写入日志内容的函数名称
     * @param fmt
     * @param ...
     */
    void LogMessage(int level, const char *file, int line, const char *function, const char *fmt, ...);
    /** 
     * @brief 设置日志的级别
     * 
     * @param level DEBUG|WARN|INFO|ERROR
     */
    void SetLogLevel(const char *level);
    /** 
     * @brief 设置日志文件的名称
     * 
     * @param filename 日志文件的名称
     */
    void SetFileName(const char *filename, bool flag = true);
    /** 
     * @brief 检测文件是否已经打开,标准输出,错误输出重定向
     */
    void CheckFile();
    void SetCheck(int v) {_check = v;}
    /** 
     * @brief 设置日志文件文件的大小,达到maxFileSize就新打开一个文件
     * 如果不设置此项，日志系统会忽略日志滚动
     * 
     * @param maxFileSize 日志文件的大小
     */
    void SetMaxFileSize( int64_t maxFileSize=0x40000000);
    /** 
     * @brief 保留最近maxFileIndex个日志文件，超出maxFileIndex个日志文件
     * 会按时间先后删除,但进程重启后日志系统会按时间先后重新统计
     * 
     * @param maxFileIndex 保留文件的最大个数
     */
    void SetMaxFileIndex( int maxFileIndex= 0x0F);
    void WriteLog(const char *buffer, int size);
private:
    Logger(const Logger& logger);
private:
    int _fd;
    char* _name;
    int _check;
    size_t _maxFileIndex;
    int64_t _maxFileSize;
    bool _flag;

public:
    int _level;

private:
    std::deque<std::string> _fileList;
    static const char* const _errstr[];   
    Mutex* _fileSizeMutex;
    Mutex* _fileIndexMutex; 
};

}

#endif //__MAFKA_LOGGER_H__
