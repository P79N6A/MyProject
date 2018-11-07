/**
* @file InfomationCollect.h
* @Brief 信息收集类相关定义  
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-14
*/

#ifndef __INFOMATIONCOLLECT_H__
#define __INFOMATIONCOLLECT_H__

#include <unistd.h>
#include <stdio.h>
#include <sys/time.h>
#include <vector>
#include <string>

///thrift
#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>
#include "../cmtrace-gen-cpp/aggregator_common_types.h"
#include "../cmtrace-gen-cpp/SGAgent.h"
#include "TaskQueue.h"
#include "RemoteProcessCall.h"
#include "ExceptionLog.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace boost;

/*-----------------------------------------------------------------------*/
enum ClientInterfaceType
{
    RPC = 1,
    LOG = 2,
    COMMLOG = 3
};

class ClientInterface
{
public:
    ClientInterface();
    
    virtual void rpcCollect(const RemoteProcessCall& rpc){};
    virtual void logCollect(const ExceptionLog& exceptionLog){};
public:
    string m_agentIp;
    int m_agentPort;
};

class RpcCollectClient : public ClientInterface
{
public:
    static void * rpcConsumer(void *args);
    static RpcCollectClient * getInstance();
	
    /**
	* @Brief 收集调用信息函数
	* @param rpc 调用信息结构体
	*/
	void rpcCollect(const RemoteProcessCall& rpc);
    
    /** 
	* @Brief 将调用信息丢入消息队列 
	* @param rpc 调用信息结构体
	* @return true标示插入成功
	*/
	bool putRpc(const RemoteProcessCall& rpc);

	/**
	* @Brief 从消息队列中取出rpc调用信息
	* @return 取出的rpc调用信息
	*/
	com::sankuai::cmtrace::SGModuleInvokeInfo getRpc();

    /**
    * @Brief 获得队列大小
    * @return 
    */
    int getSize();
public:
    TaskQueue<com::sankuai::cmtrace::SGModuleInvokeInfo> m_rpcQueue;
private:
    RpcCollectClient():m_rpcQueue(10000){};
    static RpcCollectClient * m_instance;
};

class LogCollectClient : public ClientInterface
{
public:
    static void * logConsumer(void * args);
    static LogCollectClient * getInstance();

    /**
	* @Brief 收集日志信息
	* @param exceptionLog
	*/
	void logCollect(const ExceptionLog& exceptionLog);
		    
    /**
	* @Brief 将异常日志丢入消息队列
	* @param log 异常日志调用信息结构体
	* @return true表示插入成功
	*/
	bool putLog(const ExceptionLog& log);

	/**
	* @Brief 从消息队列中取出log调用信息
	* @return 取出的log信息
	*/
	com::sankuai::cmtrace::SGLog getLog();

    /**
    * @Brief 获得队列大小
    * @return 
    */
    int getSize();
public:         
	TaskQueue<com::sankuai::cmtrace::SGLog> m_logQueue;
private:
    LogCollectClient():m_logQueue(10000){};
    static LogCollectClient * m_instance;
};

class Factory
{
public:
    static Factory * getInstance();        
    ClientInterface * getClient(int type);
private:
    Factory(){};
    static Factory * m_instance;
};

#endif
