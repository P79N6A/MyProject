/**
* @file EndPoint.h
* @Brief 端点类结构体定义
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/

#ifndef __ENDPOINT_H__
#define __ENDPOINT_H__

#include <stdint.h>
#include <string>
using namespace std;

///端点类设计
class EndPoint
{
public:
	EndPoint();
	EndPoint(const string& appKey, const string& host, int32_t port);

	const string getAppKey() const;
	const string getHost() const;
	const int32_t getPort() const;

private:
	string m_appKey;
	string m_host;
	int32_t m_port;
};

#endif
