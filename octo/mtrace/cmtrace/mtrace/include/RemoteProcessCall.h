/**
* @file RemoteProcessCall.h
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/

#ifndef __REMOTEPROCESSCALL_H_
#define __REMOTEPROCESSCALL_H_

#include <stdint.h>
#include <string>
#include "EndPoint.h"
using namespace std;

class RemoteProcessCall
{
public:
	RemoteProcessCall();
	~RemoteProcessCall();
	RemoteProcessCall(const string& traceId,const string& rpcId, const string& rpcName); ///<设置traceId,rpcId,rpcName
	RemoteProcessCall(const string& rpcName);
	
	RemoteProcessCall(int type, const string& traceId, const string& rpcId, const string& rpcName, const EndPoint& local, const EndPoint& remote, bool sample, int debug, uint64_t curTime);

	/**
	* @Brief Get nested Call's rpcId
	* @return 
	*/
	string getNextRpcId();
	void setStatusAndTime(int32_t status, uint64_t curTime);
	void setCount(int32_t count);
	void setSample(bool sample);

	const string getTraceId() const;
	const string getRpcId() const;
	const string getRpcName() const;
	
	const string getLocalAppKey() const;
	const string getLocalHost() const;
	const int32_t getLocalPort() const;
	
	const string getRemoteAppKey() const;
	const string getRemoteHost() const;
	const int32_t getRemotePort() const;
	
	const int64_t getStart() const;
	const uint32_t getCost() const;
	const int32_t getType() const;
	const int32_t getStatus() const;
	const int32_t getCount() const;
	const bool getSample() const;
	const int32_t getDebug() const;
	const string getExtend() const;

private:
	string m_traceId;
	string m_rpcId;
	string m_rpcName;
	EndPoint m_local;
	EndPoint m_remote;
	int64_t m_start;
	int32_t m_cost;
	int32_t m_type;	
	int32_t m_status;
	int32_t m_count;
	int32_t m_debug;	
	string m_extend; //reserved field

	///internal param
	uint32_t m_curSessionNum;	//accumulate call number
	int64_t m_end;
	bool m_sample; //decide whether to sample
};

#endif
