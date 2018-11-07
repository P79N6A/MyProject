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
* @brief �򵥵���־ϵͳ 
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
     * @brief ����־����д���ļ�
     * 
     * @param level ��־�ļ���
     * @param file  ��־�������ڵ��ļ�
     * @param line  ��־�������ڵ��ļ����к�
     * @param function д����־���ݵĺ�������
     * @param fmt
     * @param ...
     */
    void LogMessage(int level, const char *file, int line, const char *function, const char *fmt, ...);
    /** 
     * @brief ������־�ļ���
     * 
     * @param level DEBUG|WARN|INFO|ERROR
     */
    void SetLogLevel(const char *level);
    /** 
     * @brief ������־�ļ�������
     * 
     * @param filename ��־�ļ�������
     */
    void SetFileName(const char *filename, bool flag = true);
    /** 
     * @brief ����ļ��Ƿ��Ѿ���,��׼���,��������ض���
     */
    void CheckFile();
    void SetCheck(int v) {_check = v;}
    /** 
     * @brief ������־�ļ��ļ��Ĵ�С,�ﵽmaxFileSize���´�һ���ļ�
     * ��������ô����־ϵͳ�������־����
     * 
     * @param maxFileSize ��־�ļ��Ĵ�С
     */
    void SetMaxFileSize( int64_t maxFileSize=0x40000000);
    /** 
     * @brief �������maxFileIndex����־�ļ�������maxFileIndex����־�ļ�
     * �ᰴʱ���Ⱥ�ɾ��,��������������־ϵͳ�ᰴʱ���Ⱥ�����ͳ��
     * 
     * @param maxFileIndex �����ļ���������
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
