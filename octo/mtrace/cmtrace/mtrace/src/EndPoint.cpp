/**
* @file EndPoint.cpp
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/
#include "EndPoint.h"

EndPoint::EndPoint():m_appKey(""), m_host(""), m_port(0)
{

}

EndPoint::EndPoint(const string& appKey, const string& host, int32_t port):m_appKey(appKey), m_host(host), m_port(port)
{

}

const string EndPoint::getAppKey() const
{
	return m_appKey;
}

const string EndPoint::getHost() const
{
	return m_host;
}

const int32_t EndPoint::getPort() const
{
	return m_port;
}
