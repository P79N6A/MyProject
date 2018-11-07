/**
* @file ExceptionLog.h
* @Brief 异常日志结构类定义
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-20
*/
#ifndef __EXCEPTIONLOG_H__
#define __EXCEPTIONLOG_H__

#include <unistd.h>
#include <string>
#include <stdint.h>
using namespace std;

class ExceptionLog
{
public:
	ExceptionLog();
	ExceptionLog(const string& appKey, const string& content, int32_t level, uint64_t curTime);

	const string getAppKey() const;
	const uint64_t getTime() const;
	const int32_t getLevel() const;
	const string getContent() const;	
private:	
	string m_appKey;
	uint64_t m_time;
	int32_t m_level;
	string m_content;
};

#endif
