/**
* @file InfomationCollect.cpp
* @Brief 线上直连本机，线下连接到192.168.3.163 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-14
*/

#include <sys/time.h>
#include <pthread.h>
#include <string.h>
#include <string>
#include <map>
#include <typeinfo>
#include "InfomationCollect.h"
#include "CommonTools.h"
#include "MTrace.h"
#include "Serialize.h"

static pthread_mutex_t g_RpcCollectClientMutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t g_LogCollectClientMutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t g_FactoryMutex = PTHREAD_MUTEX_INITIALIZER;
RpcCollectClient * RpcCollectClient::m_instance = NULL;
LogCollectClient * LogCollectClient::m_instance = NULL;

Factory * Factory::m_instance = NULL;

ClientInterface::ClientInterface():m_agentPort(5266)
{
    string localIp = TraceNameSpace::m_localIp;
    int pos = localIp.find("10"); //10打头的都是线上机器
    if(0 == pos)
    {
        m_agentIp = localIp;
    }
    else
    {
        m_agentIp = "10.4.246.240";   //线下机统一打到这台机器
    }
}

/* 接口1：RpcCollectClient*/
RpcCollectClient * RpcCollectClient::getInstance()
{ 
    if(NULL == m_instance)
    {
		pthread_mutex_lock(&g_RpcCollectClientMutex);
        if(NULL == m_instance)
        {
            m_instance = new RpcCollectClient;
            pthread_t tidRpc;
            pthread_create(&tidRpc, NULL, rpcConsumer, NULL);
        }         
		pthread_mutex_unlock(&g_RpcCollectClientMutex);
    }
    return m_instance;
}

void * RpcCollectClient::rpcConsumer(void * args)
{
	RpcCollectClient * pRpcCollectClient = RpcCollectClient::getInstance();
    shared_ptr<TSocket> pSocket(new TSocket(getSgAgentIp(), SgAgentPort));
    COMM_DEBUG("[rpcConsumer] init sg_agent client, ip:port = %s:%d", getSgAgentIp().c_str(), SgAgentPort);
    pSocket->setConnTimeout(100);
    pSocket->setSendTimeout(100);
    pSocket->setRecvTimeout(100);
    shared_ptr<TFramedTransport> pFramedTransport(new TFramedTransport(pSocket));
    shared_ptr<TBinaryProtocol> pProtocol(new TBinaryProtocol(pFramedTransport));
    com::sankuai::cmtrace::SGAgentClient client(pProtocol);

    while(1)
	{	
		int retrycount = 0;
		while(retrycount < 3)
		{
			try
            {
                pFramedTransport->open(); ///< thrift 底层会判断当前socket是否可用，不可用再创建。
                const com::sankuai::cmtrace::SGModuleInvokeInfo & sgModuleInvokeInfo = pRpcCollectClient->getRpc();
                COMM_DEBUG("[rpcConsumer]get SUCCESS, remainder size:%d", pRpcCollectClient->getSize());
                int ret = client.uploadModuleInvoke(sgModuleInvokeInfo);
                COMM_DEBUG("[rpcConsumer] call uploadModuleInvoke SUCCESS, ret=%d", ret);
                break;
			}
			catch(TException &t)
			{
				// warning log
                COMM_DEBUG("[rpcConsumer]WARNING:%s", t.what());
                pFramedTransport->close();
			}
			retrycount++;
			usleep(SLEEPTIME); 
		}
	}
    return (void *)0;
}

void RpcCollectClient::rpcCollect(const RemoteProcessCall & rpc)
{
    if(SUCCESS == putRpc(rpc))
    {
        //TODO
        COMM_DEBUG("[rpcCollect]put SUCCESS!");
    }
    else
    {
        COMM_DEBUG("[rpcCollect]put FAIL!");
        /*error info;
        string content = "ip:" + TraceNameSpace::m_localIp + "|msg:rpc Pool is Full"; 
        int32_t level = 1;
        Trace::getInstance()->sendLog(rpc.getLocalAppKey(), content, level);
        */
    }
}

bool RpcCollectClient::putRpc(const RemoteProcessCall& rpc)
{
    com::sankuai::cmtrace::SGModuleInvokeInfo sgModuleInvokeInfo;
	sgModuleInvokeInfo.__set_traceId(rpc.getTraceId());
	sgModuleInvokeInfo.__set_spanId(rpc.getRpcId());
	sgModuleInvokeInfo.__set_spanName(rpc.getRpcName());
	sgModuleInvokeInfo.__set_localAppKey(rpc.getLocalAppKey());
	sgModuleInvokeInfo.__set_localHost(rpc.getLocalHost());
	sgModuleInvokeInfo.__set_localPort(rpc.getLocalPort());
	sgModuleInvokeInfo.__set_remoteAppKey(rpc.getRemoteAppKey());
	sgModuleInvokeInfo.__set_remoteHost(rpc.getRemoteHost());
	sgModuleInvokeInfo.__set_remotePort(rpc.getRemotePort());
	sgModuleInvokeInfo.__set_start(rpc.getStart());
	sgModuleInvokeInfo.__set_cost(rpc.getCost());
	sgModuleInvokeInfo.__set_type(rpc.getType());
	sgModuleInvokeInfo.__set_status(rpc.getStatus());
	sgModuleInvokeInfo.__set_count(rpc.getCount());
	sgModuleInvokeInfo.__set_debug(rpc.getDebug());
	sgModuleInvokeInfo.__set_extend(rpc.getExtend());

	return m_rpcQueue.put(sgModuleInvokeInfo);
}

com::sankuai::cmtrace::SGModuleInvokeInfo RpcCollectClient::getRpc()
{
	return m_rpcQueue.get();
}

int RpcCollectClient::getSize()
{
    return m_rpcQueue.size();
}

/* 接口2：LogCollectClient*/
LogCollectClient * LogCollectClient::getInstance()
{
    if(NULL == m_instance)
    {
        pthread_mutex_lock(&g_LogCollectClientMutex);
        if(NULL == m_instance)
        {
            m_instance = new LogCollectClient;
            pthread_t tidLog;
            pthread_create(&tidLog, NULL, logConsumer, NULL);
        }
        pthread_mutex_unlock(&g_LogCollectClientMutex);
    }   
    return m_instance;
}

void * LogCollectClient::logConsumer(void * args)
{
	LogCollectClient * pLogCollectClient = LogCollectClient::getInstance();
    shared_ptr<TSocket> pSocket(new TSocket(getSgAgentIp(), SgAgentPort));
    pSocket->setConnTimeout(100);
    pSocket->setSendTimeout(100);
    pSocket->setRecvTimeout(100);
    shared_ptr<TFramedTransport> pFramedTransport(new TFramedTransport(pSocket));
    shared_ptr<TBinaryProtocol> pProtocol(new TBinaryProtocol(pFramedTransport));
    com::sankuai::cmtrace::SGAgentClient client(pProtocol);

    while(1)
	{	
		int retrycount = 0;
		while(retrycount < 3)
		{
			try
            {
                pFramedTransport->open(); ///< thrift 底层会判断当前socket是否可用，不可用再创建。                
                const com::sankuai::cmtrace::SGLog & sgLog = pLogCollectClient->getLog();
				client.uploadLog(sgLog);
                COMM_DEBUG("[logConsumer]get SUCCESS! remainder size:%d", pLogCollectClient->getSize());
                break;
			}
			catch(TException &t)
			{
				// warning log
                COMM_DEBUG("[logConsumer]WARNING:%s", t.what());
                pFramedTransport->close();
			}
			retrycount++;
			usleep(SLEEPTIME); 
		}
	}
    return (void *)0;
}

void LogCollectClient::logCollect(const ExceptionLog& log)
{
    if(SUCCESS == putLog(log))
    {
        COMM_DEBUG("[logCollect]put SUCCESS!");
    }
    else
    {
        COMM_DEBUG("[logCollect]put FAIL!");
        //error info; 本身就是异常日志，不再放入异常日志，以免递归
    }
}

bool LogCollectClient::putLog(const ExceptionLog& exceptionLog)
{
    com::sankuai::cmtrace::SGLog sgLog;
	sgLog.__set_appkey(exceptionLog.getAppKey());
	sgLog.__set_content(exceptionLog.getContent());
	sgLog.__set_level(exceptionLog.getLevel());
	sgLog.__set_time(exceptionLog.getTime());

	return m_logQueue.put(sgLog);
}

com::sankuai::cmtrace::SGLog LogCollectClient::getLog()
{
	return m_logQueue.get();	
}

int LogCollectClient::getSize()
{
    return m_logQueue.size();
}

/*工厂相关*/
Factory * Factory::getInstance()
{
    if(NULL == m_instance)
    {
        pthread_mutex_lock(&g_FactoryMutex);
        if(NULL == m_instance)
        {
            m_instance = new Factory;
        }
        pthread_mutex_unlock(&g_FactoryMutex);
    }
    return m_instance;
}

ClientInterface * Factory::getClient(int type)
{
    if(RPC == type)
    {
        return RpcCollectClient::getInstance();
    }
    else if(LOG == type)
    {
        return LogCollectClient::getInstance();
    }
    return NULL;
}
