#include <stdio.h>
#include "ExceptionLog.h"

ExceptionLog::ExceptionLog():m_level(0), m_time(0)
{

}

ExceptionLog::ExceptionLog(const string& appKey, const string& content, int32_t level, uint64_t curTime)
{
	m_appKey = appKey;
	m_time = curTime;
	m_content = content;
	m_level = level;
}

const string ExceptionLog::getAppKey() const
{
	return m_appKey;	
}

const uint64_t ExceptionLog::getTime() const
{
	return m_time;
}

const int32_t ExceptionLog::getLevel() const
{
	return m_level;
}

const string ExceptionLog::getContent() const
{
	return m_content;
}
