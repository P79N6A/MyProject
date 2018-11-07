/**
* @file RemoteProcessCall.cpp
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/

#include <stdio.h>
#include <string.h>
#include <uuid/uuid.h>
#include "RemoteProcessCall.h"

/**
* @Brief 将系统的uuid作为rpcId
* @param uuid_str
*/
static void get_uuid(string &uuid_str)
{	
    uuid_t uuid;
    char str[128] = {0};

    uuid_generate(uuid);
    uuid_unparse(uuid, str);
	uuid_str = str;
	return; 
}

RemoteProcessCall::RemoteProcessCall():m_traceId("0"), m_rpcId("0"), m_rpcName(""), m_start(0), m_cost(0), m_type(0), m_status(0), m_count(0), m_debug(0), m_extend(""), m_curSessionNum(0), m_end(0), m_sample(false)
{

}

RemoteProcessCall::~RemoteProcessCall()
{

}

/**
* @Brief 这个构造函数主要针对于callee,有调用方传递过来的traceId，rpcId
* @param traceId
* @param rpcId
* @param traceId
*/
RemoteProcessCall::RemoteProcessCall(const string& traceId,const string& rpcId,const string& rpcName):m_traceId(traceId), m_rpcId(rpcId), m_rpcName(rpcName), m_start(0), m_cost(0), m_type(0), m_status(0), m_count(0), m_debug(0), m_extend(""), m_curSessionNum(0), m_end(0), m_sample(false)
{

}

/**
* @Brief 这个构造函数主要针对caller,自己生成traceId，rpcId
* @param rpcName
*/
RemoteProcessCall::RemoteProcessCall(const string& rpcName):m_traceId("0"), m_rpcId("0"), m_rpcName(rpcName), m_start(0), m_cost(0), m_type(0), m_status(0), m_count(0), m_debug(0), m_extend(""), m_curSessionNum(0), m_end(0), m_sample(false)
{
	get_uuid(m_traceId);
}

RemoteProcessCall::RemoteProcessCall(int type, const string& traceId, const string& rpcId, const string& rpcName, const EndPoint& local, const EndPoint& remote, bool sample, int debug, uint64_t curTime):m_traceId("0"), m_rpcId("0"), m_rpcName(rpcName), m_start(0), m_cost(0), m_type(0), m_status(0), m_count(0), m_debug(0), m_extend(""), m_curSessionNum(0), m_end(0), m_sample(false)
{
	m_type = type;

	if(traceId.empty())
	{
		get_uuid(m_traceId);
	}
	else
	{
		m_traceId = traceId;
	}

	if(rpcId.empty())
	{
		m_rpcId = string("0");
	}
	else
	{
		m_rpcId = rpcId;
	}

	m_rpcName = rpcName;
	m_local = local;
	m_remote = remote;
	m_sample = sample;
	m_debug = debug;
	m_start = curTime;
}

string RemoteProcessCall::getNextRpcId()
{
	++m_curSessionNum;
	char buf[64] = {0};
	snprintf(buf, sizeof(buf), "%d", m_curSessionNum);
	string rpcId = m_rpcId + "." + buf;
	return rpcId;
}

void RemoteProcessCall::setStatusAndTime(int32_t status, uint64_t curTime)
{
	m_status = status;
	m_end = curTime;
    m_cost = m_end - m_start;
	return;
}

void RemoteProcessCall::setCount(int32_t count)
{
	m_count = count;
	return;
}

void RemoteProcessCall::setSample(bool sample)
{
	m_sample = sample;
	return;
}

const bool RemoteProcessCall::getSample() const
{
	return m_sample;
}

const string RemoteProcessCall::getTraceId() const
{
	return m_traceId;
}

const string RemoteProcessCall::getRpcId() const
{
	return m_rpcId;
}

const string RemoteProcessCall::getRpcName() const
{
	return m_rpcName;
}

const string RemoteProcessCall::getLocalAppKey() const
{
	return m_local.getAppKey();
}

const string RemoteProcessCall::getLocalHost() const
{
	return m_local.getHost();
}

const int32_t RemoteProcessCall::getLocalPort() const
{
	return m_local.getPort();
}

const string RemoteProcessCall::getRemoteAppKey() const
{
	return m_remote.getAppKey();
}

const string RemoteProcessCall::getRemoteHost() const
{
	return m_remote.getHost();
}

const int32_t RemoteProcessCall::getRemotePort() const
{
	return m_remote.getPort();
}

const int64_t RemoteProcessCall::getStart() const
{
	return m_start;
}

const uint32_t RemoteProcessCall::getCost() const
{
	return m_end - m_start;
}

const int32_t RemoteProcessCall::getType() const
{
	return m_type;	
}

const int32_t RemoteProcessCall::getStatus() const
{
	return m_status;
}

const int32_t RemoteProcessCall::getCount() const
{
	return m_count;
}

const int32_t RemoteProcessCall::getDebug() const
{
	return m_debug;
}

const string RemoteProcessCall::getExtend() const
{
	return m_extend;
}
