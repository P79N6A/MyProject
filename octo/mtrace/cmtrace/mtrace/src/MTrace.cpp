/**
* @file MTrace.cpp
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-12
*/
#include <sys/time.h>
#include <arpa/inet.h>
#include <boost/lexical_cast.hpp>
#include <string.h>
#include "../cmtrace-gen-cpp/aggregator_common_constants.h"
#include "../cmtrace-gen-cpp/aggregator_common_types.h"
#include "MTrace.h"
#include "InfomationCollect.h"
#include "Sample.h"
#include "CommonTools.h"
#include "CommonCollect.h"

using namespace std;

Trace * Trace::m_instance = NULL;
static pthread_mutex_t g_traceMutex = PTHREAD_MUTEX_INITIALIZER;
static int clientInitFlag = 0;
static int serverInitFlag = 0; 

static void thresholdlogcpy(com::sankuai::cmtrace::TraceThresholdLog &log, const RemoteProcessCall &rpc)
{
	log.__set_traceId(rpc.getTraceId());
	log.__set_spanId(rpc.getRpcId());
	log.__set_spanName(rpc.getRpcName());
	log.__set_localAppKey(rpc.getLocalAppKey());
	log.__set_localHost(rpc.getLocalHost());
	log.__set_localPort(rpc.getLocalPort());
	log.__set_remoteAppKey(rpc.getRemoteAppKey());
	log.__set_remoteHost(rpc.getRemoteHost());
	log.__set_remotePort(rpc.getRemotePort());
	log.__set_start(rpc.getStart());
	log.__set_cost(rpc.getCost());
	log.__set_type(rpc.getType());
	log.__set_status(rpc.getStatus());
	log.__set_count(rpc.getCount());
	log.__set_debug(rpc.getDebug());
	log.__set_extend(rpc.getExtend());
}

namespace TraceNameSpace
{
    string m_localIp = "127.0.0.1";
    bool m_init = false;
	pthread_once_t once_control = PTHREAD_ONCE_INIT;
	pthread_key_t m_client;
	pthread_key_t m_server;
	pthread_key_t m_debug;

    pthread_key_t m_clientLocal;
    pthread_key_t m_clientRemoteAppKey;
    
    pthread_key_t m_serverLocal;
    pthread_key_t m_serverRemoteAppKey;

    Configure CONFIG;
	
    //这个地方使用到两个只执行一次的函数，一种是pthread_once, 还有一种是cas操作，保证在多线程情况下也安全
	void clientInit(const string& localAppKey, const string& remoteAppKey)
	{
		pthread_once(&once_control, tlsInit);

        EndPoint * pClientLocal = new EndPoint(localAppKey, TraceNameSpace::m_localIp, 0);
        setClientLocal(pClientLocal);

        string * pClientRemoteAppKey = new string(remoteAppKey);
        setClientRemoteAppKey(pClientRemoteAppKey);
        return;
	}

    void serverInit(const string& localAppKey, const string& remoteAppKey)
    {        
		pthread_once(&once_control, tlsInit);
        
        EndPoint * pServerLocal = new EndPoint(localAppKey, TraceNameSpace::m_localIp, 0);
        setServerLocal(pServerLocal);

        string * pServerRemoteAppKey = new string(remoteAppKey);
        setServerRemoteAppKey(pServerRemoteAppKey);
    }

    void tlsInit()
    {
        m_init = true;
        m_localIp = getMachineIp();
        pthread_key_create(&m_client, NULL);
        pthread_key_create(&m_server, NULL);
        pthread_key_create(&m_debug, NULL);

        pthread_key_create(&m_clientRemoteAppKey, NULL);
        pthread_key_create(&m_clientLocal, NULL);

        pthread_key_create(&m_serverRemoteAppKey, NULL);
        pthread_key_create(&m_serverLocal, NULL);
        return;
    }

	void setDebug(int debug)
	{
		clearDebug(); ///< avoid memory leak
		int *pDebug = new int(debug);
		pthread_setspecific(m_debug, (void *)pDebug);
        g_level = 1;  ///< control debug log
	}

	int * getDebug()
	{
		return (int*)pthread_getspecific(m_debug);
	}
	
	void clearDebug()
	{
		int * pDebug = getDebug();
		if(NULL != pDebug)
		{
			delete pDebug;
			pDebug = NULL;
			pthread_setspecific(m_debug, NULL);
		}
        g_level = 0;
	}

    /**
    * @Brief server Rpc相关
    * @param pRpc
    */
	void setServerRpc(RemoteProcessCall* pRpc)
	{
        clearServerRpc();
		pthread_setspecific(m_server, (void *)pRpc);
	}

	RemoteProcessCall * getServerRpc()
	{
		return (RemoteProcessCall*)pthread_getspecific(m_server);
	}

	void clearServerRpc()
	{
		RemoteProcessCall * pRpc = getServerRpc();
		if(NULL != pRpc)
		{
			delete pRpc;
			pRpc = NULL;
			pthread_setspecific(m_server, NULL);
		}
	}

    /**
    * @Brief client Rpc 相关
    * @param pRpc
    */
	void setClientRpc(RemoteProcessCall * pRpc)
	{
        clearClientRpc();
		pthread_setspecific(m_client, (void*)pRpc);
	}

	RemoteProcessCall * getClientRpc()
	{
		return (RemoteProcessCall*)pthread_getspecific(m_client);
	}

	void clearClientRpc()
	{
		RemoteProcessCall * pRpc = getClientRpc();
		if(NULL != pRpc)
		{
			delete pRpc;
			pRpc = NULL;
			pthread_setspecific(m_client, NULL);
		}
	}
    
    /**
    * @Brief client Local 相关
    * @param pLocal
    */
    void setClientLocal(EndPoint * pLocal)
    {
        clearClientLocal();
        pthread_setspecific(m_clientLocal, pLocal);
    }

    EndPoint * getClientLocal()
    {
        return (EndPoint *)pthread_getspecific(m_clientLocal);
    }

    void clearClientLocal()
    {
        EndPoint * pLocal = getClientLocal();
        if(NULL != pLocal)
        {
            delete pLocal;
            pLocal = NULL;
            pthread_setspecific(m_clientLocal, NULL);
        }
    }

    /**
    * @Brief server local 相关
    * @param pLocal
    */
    void setServerLocal(EndPoint * pLocal)
    {
        clearServerLocal();
        pthread_setspecific(m_serverLocal, pLocal);
    }

    EndPoint * getServerLocal()
    {
        return (EndPoint *)pthread_getspecific(m_serverLocal);
    }

    void clearServerLocal()
    {
        EndPoint * pLocal = getServerLocal();
        if(NULL != pLocal)
        {
            delete pLocal;
            pLocal = NULL;
            pthread_setspecific(m_serverLocal, NULL);
        }
    }

    /**
    * @Brief client remote 相关 
    * @param pLocal
    */
    void setClientRemoteAppKey(string * pClientRemoteAppKey)
    {
        clearClientRemoteAppKey();
        pthread_setspecific(m_clientRemoteAppKey, pClientRemoteAppKey);
    }

    string * getClientRemoteAppKey()
    {
        return (string *)pthread_getspecific(m_clientRemoteAppKey);
    }

    void clearClientRemoteAppKey()
    {
        string * pClientRemoteAppKey = getClientRemoteAppKey();
        if(NULL != pClientRemoteAppKey)
        {
            delete pClientRemoteAppKey;
            pClientRemoteAppKey = NULL;
            pthread_setspecific(m_clientRemoteAppKey, NULL);
        }
    }

    /**
    * @Brief server remote 相关 
    * @param pLocal
    */
    void setServerRemoteAppKey(string * pServerRemoteAppKey)
    {
        clearServerRemoteAppKey();
        pthread_setspecific(m_serverRemoteAppKey, pServerRemoteAppKey);
    }

    string * getServerRemoteAppKey()
    {
        return (string *)pthread_getspecific(m_serverRemoteAppKey);
    }

    void clearServerRemoteAppKey()
    {
        string * pServerRemoteAppKey = getServerRemoteAppKey();
        if(NULL != pServerRemoteAppKey)
        {
            delete pServerRemoteAppKey;
            pServerRemoteAppKey = NULL;
            pthread_setspecific(m_serverRemoteAppKey, NULL);
        }
    }

    Configure::Configure()
    {
        pthread_rwlock_init(&rwlock, NULL);
        identify2Limit.clear();
    }

    void Configure::setThreshold(string appKey, string spanName, int threshold)
    {
        pthread_rwlock_wrlock(&rwlock);
        string appKeyAndName = appKey + "\t" + spanName;
        identify2Limit.insert(make_pair<string, int>(appKeyAndName, threshold));
        pthread_rwlock_unlock(&rwlock);
        return; 
    }

    void Configure::setThreshold(string appKey, int threshold)
    {
        pthread_rwlock_wrlock(&rwlock);
        identify2Limit.insert(make_pair<string, int>(appKey, threshold));
        pthread_rwlock_unlock(&rwlock);
        return;
    }
}

Trace * Trace::getInstance()
{
	if(NULL == m_instance)
	{
		pthread_mutex_lock(&g_traceMutex);
		if(NULL == m_instance)
		{
			m_instance = new Trace;
		}
		pthread_mutex_unlock(&g_traceMutex);
	}
	return m_instance;
}

int Trace::sendRpc(int type, const EndPoint& local, const EndPoint& remote, string rpcName, string traceId, string rpcId, bool sample, int debug, string extend)
{
    if(!TraceNameSpace::m_init)
    {
        COMM_DEBUG("not Init before use");
        return -1;
    }

	uint64_t curTime = getCurrentMilliTime();
	int *pDebug = TraceNameSpace::getDebug();
	if(MTRACE_CLIENT == type)
	{
		int newDebug = 0;
        bool newSample = false;
        
        newDebug = (NULL != pDebug ?  *pDebug : 0);

        if(pDebug && ADD_DEBUG_LOG_FULLSAMPLE == *pDebug) ///<开发人员调试参数
        {
            newSample = true;
        }

		RemoteProcessCall * pServerRpc = TraceNameSpace::getServerRpc();
		RemoteProcessCall * pClientRpc = NULL;
		if(NULL == pServerRpc)
		{
			pClientRpc = new RemoteProcessCall(type, traceId, rpcId, rpcName, local, remote, newSample, newDebug, curTime);
		}
		else
		{
			string newTraceId = pServerRpc->getNextRpcId();
			pClientRpc = new RemoteProcessCall(type, /*discard in param traceId*/newTraceId, rpcId, rpcName, local, remote, newSample, newDebug, curTime);
		}
		TraceNameSpace::setClientRpc(pClientRpc);					
		
        if(pClientRpc)
        {
            COMM_DEBUG("[Trace::sendRpc]traceId:%s\trpcId:%s\trpcName:%s\
                \tlocalAppKey:%s\tlocalHost:%s\tlocalPort:%d\tremoteAppKey:%s\
                \tremoteHost:%s\tremotePort:%d\tstart:%llu\ttype:%d\tstatus:%d\
                \tdebug:%d\tsample:%d\textend:%s",\
                pClientRpc->getTraceId().c_str(), pClientRpc->getRpcId().c_str(),\
                pClientRpc->getRpcName().c_str(), pClientRpc->getLocalAppKey().c_str(),\
                pClientRpc->getLocalHost().c_str(), pClientRpc->getLocalPort(),\
                pClientRpc->getRemoteAppKey().c_str(), pClientRpc->getRemoteHost().c_str(),\
                pClientRpc->getRemotePort(), pClientRpc->getStart(), pClientRpc->getType(),\
                pClientRpc->getStatus(), pClientRpc->getDebug(),\
                pClientRpc->getSample(), pClientRpc->getExtend().c_str());
        }
    }
	else if(MTRACE_SERVER == type)
	{
        int newDebug = 0;
        bool newSample = false;
        if(0 != debug) ///<只有服务端依赖调用链上传递过来的debug
        {
            newDebug = 1;
        }
        else
        {
            newDebug = (NULL != pDebug ? *pDebug : 0);
        }
        
        if(false != sample) ///<只有服务端依赖调用链上传递过来的sample
        {
            newSample = true;
        }
        else
        {
            if(pDebug && ADD_DEBUG_LOG_FULLSAMPLE == *pDebug) ///<开发人员调试参数
            {
                newSample = true;
            }
        }

		RemoteProcessCall * pServerRpc = new RemoteProcessCall(type, traceId, rpcId, rpcName, local, remote, newSample, newDebug, curTime);
		TraceNameSpace::setServerRpc(pServerRpc);

        if(pServerRpc)
        {
            COMM_DEBUG("[Trace::sendRpc]traceId:%s\trpcId:%s\trpcName:%s\
            \tlocalAppKey:%s\tlocalHost:%s\tlocalPort:%d\tremoteAppKey:%s\
            \tremoteHost:%s\tremotePort:%d\tstart:%llu\ttype:%d\tstatus:%d\
            \tdebug:%d\tsample:%d\textend:%s",\
            pServerRpc->getTraceId().c_str(), pServerRpc->getRpcId().c_str(),\
            pServerRpc->getRpcName().c_str(), pServerRpc->getLocalAppKey().c_str(),\
            pServerRpc->getLocalHost().c_str(), pServerRpc->getLocalPort(),\
            pServerRpc->getRemoteAppKey().c_str(), pServerRpc->getRemoteHost().c_str(),\
            pServerRpc->getRemotePort(), pServerRpc->getStart(), pServerRpc->getType(),\
            pServerRpc->getStatus(), pServerRpc->getDebug(),\
            pServerRpc->getSample(), pServerRpc->getExtend().c_str());
        }
    }
	return 0;
}
                       
//return 0 if succeed, return negtive if error occured
int getIpAndPort( const string& ipAndPort, string& ip, int& port) {
   if (ipAndPort.empty()) {
       return -1;
   }
   int separtorIndex = (int)ipAndPort.find_first_of(":");
   if (separtorIndex==string::npos) {
       return -1; //invalid format
   }
   string tmpIP = ipAndPort.substr( 0, separtorIndex);
   if (INADDR_NONE==inet_addr(tmpIP.c_str())) {
       return -2; //invalid ip
   }
   ip = tmpIP;
   
   string tmpPort = ipAndPort.substr( separtorIndex+1);
   try {
       port = boost::lexical_cast<int>( tmpPort);
   } catch(boost::bad_lexical_cast& e) {
       return -3; //invalid port
   }
   
   return 0;
}

int Trace::sendRpc(int type, const string& ipAndPort, const string& rpcName, string traceId, string rpcId, bool sample, int debug, string extend)
{
    if(!TraceNameSpace::m_init)
    {
        COMM_DEBUG("not Init before use");
        return -1;
    }

    string ipString = "";
    int port = 0;
    if( getIpAndPort(ipAndPort, ipString, port) <0) {
        COMM_DEBUG("[Trace::sendRpc]ERROR: illegal input ipAndPort %s", ipAndPort.c_str());
        return -1;
    }
	
	if(MTRACE_CLIENT == type)
	{
        string * pClientRemoteAppKey = TraceNameSpace::getClientRemoteAppKey();
        if(pClientRemoteAppKey)
		{
            EndPoint remoteEndPoint(*pClientRemoteAppKey, ipString, port);
            EndPoint * pClientLocal = TraceNameSpace::getClientLocal();        
            if(pClientLocal)
            {
                sendRpc(type, *pClientLocal, remoteEndPoint, rpcName, traceId, rpcId, sample, debug, extend);
            }
            else
            {
                COMM_DEBUG("[Trace::sendRpc]ERROR: not set clientLocal");
                return -1;
            }
        }
        else
        {        
            COMM_DEBUG("[Trace::sendRpc]ERROR: not set clientRemoteAppKey");
            return -1;
        }
	}
	else if(MTRACE_SERVER == type)
	{
        string * pServerRemoteAppKey = TraceNameSpace::getServerRemoteAppKey();
        if(pServerRemoteAppKey)
		{
            EndPoint remoteEndPoint(*pServerRemoteAppKey, ipString, port);
            EndPoint * pServerLocal = TraceNameSpace::getServerLocal();
            if(pServerLocal)
            {
                sendRpc(type, *pServerLocal, remoteEndPoint, rpcName, traceId, rpcId, sample, debug, extend);
            }
            else
            {
                COMM_DEBUG("[Trace::sendRpc]ERROR: not set serverLocal");
                return -1;
            }
        }
        else
        {        
            COMM_DEBUG("[Trace::sendRpc]ERROR: not set serverRemoteAppKey");
            return -1;
        }
		
    }
	return 0;
}

                       
int Trace::sendRpcWithAppkey(int type, const string& appkey, const string& ip, int port, const string& rpcName, string traceId, string rpcId, bool sample, int debug, string extend)
{
    if(!TraceNameSpace::m_init) {
        COMM_DEBUG("not Init before use");
        return -1;
    }
                
    if( ip.empty() || INADDR_NONE==inet_addr(ip.c_str())) {
        COMM_DEBUG("[Trace::sendRpc]ERROR: illegal input ip %s", ip.c_str());
        return -1;
    }
    if (port<=0) {
        COMM_DEBUG("[Trace::sendRpc]ERROR: illegal input port %d", port);
        return -1;
    }
    if (appkey.empty()) {
        COMM_DEBUG("[Trace::sendRpc]ERROR: illegal input appkey %s", appkey.c_str());
        return -1;
    }
    
    if(MTRACE_CLIENT == type)
    {
        string * pClientRemoteAppKey = TraceNameSpace::getClientRemoteAppKey();
        if(pClientRemoteAppKey)
        {
            EndPoint remoteEndPoint( appkey, ip, port);
            EndPoint * pClientLocal = TraceNameSpace::getClientLocal();
            if(pClientLocal)
            {
                sendRpc(type, *pClientLocal, remoteEndPoint, rpcName, traceId, rpcId, sample, debug, extend);
            }
            else
            {
                COMM_DEBUG("[Trace::sendRpc]ERROR: not set clientLocal");
                return -1;
            }
        }
        else
        {
            COMM_DEBUG("[Trace::sendRpc]ERROR: not set clientRemoteAppKey");
            return -1;
        }
    }
    else if(MTRACE_SERVER == type)
    {
        string * pServerRemoteAppKey = TraceNameSpace::getServerRemoteAppKey();
        if(pServerRemoteAppKey)
        {
            EndPoint remoteEndPoint( appkey, ip, port);
            EndPoint * pServerLocal = TraceNameSpace::getServerLocal();
            if(pServerLocal)
            {
                sendRpc(type, *pServerLocal, remoteEndPoint, rpcName, traceId, rpcId, sample, debug, extend);
            }
            else
            {
                COMM_DEBUG("[Trace::sendRpc]ERROR: not set serverLocal");
                return -1;
            }
        }
        else
        {
            COMM_DEBUG("[Trace::sendRpc]ERROR: not set serverRemoteAppKey");
            return -1;
        }
        
    }
    return 0;
}

int Trace::recvRpc(int type, int status, const char * fileName)
{
    if(!TraceNameSpace::m_init)
    {
        COMM_DEBUG("not Init before use");
        return -1;
    }

	uint64_t curTime = getCurrentMilliTime();
	if(MTRACE_CLIENT == type)
	{
		RemoteProcessCall * pClientRpc = TraceNameSpace::getClientRpc();
		if(pClientRpc)
		{
			pClientRpc->setStatusAndTime(status, curTime);
			Sample::getInstance()->judgeSample(*pClientRpc);	 //采样判断

			if(pClientRpc->getSample())
			{
				///rpcCollect
				Factory::getInstance()->getClient(RPC)->rpcCollect(*pClientRpc);
            
                COMM_DEBUG("[Trace::recvRpc]traceId:%s\trpcId:%s\trpcName:%s\
                    \tlocalAppKey:%s\tlocalHost:%s\tlocalPort:%d\tremoteAppKey:%s\
                    \tremoteHost:%s\tremotePort:%d\tstart:%llu\ttype:%d\tstatus:%d\
                    \tcount:%d\tdebug:%d\tsample:%d\textend:%s\tcost:%u",\
                    pClientRpc->getTraceId().c_str(), pClientRpc->getRpcId().c_str(),\
                    pClientRpc->getRpcName().c_str(), pClientRpc->getLocalAppKey().c_str(),\
                    pClientRpc->getLocalHost().c_str(), pClientRpc->getLocalPort(),\
                    pClientRpc->getRemoteAppKey().c_str(), pClientRpc->getRemoteHost().c_str(),\
                    pClientRpc->getRemotePort(), pClientRpc->getStart(), pClientRpc->getType(),\
                    pClientRpc->getStatus(), pClientRpc->getCount(), pClientRpc->getDebug(),\
                    pClientRpc->getSample(), pClientRpc->getExtend().c_str(), pClientRpc->getCost());
            }
            
            //慢查询相关逻辑 
            int cost = pClientRpc->getCost();
            string appKey = pClientRpc->getRemoteAppKey(); 
            string rpcName = pClientRpc->getRpcName();
            string appKeyAndName = appKey + "\t" + rpcName;

            pthread_rwlock_rdlock(&((TraceNameSpace::CONFIG).rwlock));
            map<string, int> sMap = (TraceNameSpace::CONFIG).identify2Limit; 
            map<string, int>::iterator iter = sMap.find(appKeyAndName);
            bool satisfy = (sMap.end() != iter || sMap.end() != (iter = sMap.find(appKey))) && (cost > iter->second);
            pthread_rwlock_unlock(&((TraceNameSpace::CONFIG).rwlock));
            
            if(satisfy)
            {
                CommonContentCollectClient<com::sankuai::cmtrace::TraceThresholdLog> * pCommClient = Singleton< CommonContentCollectClient<com::sankuai::cmtrace::TraceThresholdLog> >::getInstance();
                com::sankuai::cmtrace::TraceThresholdLog log;
                thresholdlogcpy(log, *pClientRpc);
                pCommClient->commonContentCollect(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST, log);

                COMM_DEBUG("[Trace::recvRpc]slowQuery:%d", cost); 
            }
        
			TraceNameSpace::clearClientRpc(); 
		}
	}
	else if(MTRACE_SERVER == type)
	{
		RemoteProcessCall * pServerRpc = TraceNameSpace::getServerRpc();
		if(pServerRpc)
		{
			pServerRpc->setStatusAndTime(status, curTime);
			Sample::getInstance()->judgeSample(*pServerRpc);	//采样判断
			if(pServerRpc->getSample())
			{
				///rpcCollect
				Factory::getInstance()->getClient(RPC)->rpcCollect(*pServerRpc);
				
                COMM_DEBUG("[Trace::recvRpc]traceId:%s\trpcId:%s\trpcName:%s\
                    \tlocalAppKey:%s\tlocalHost:%s\tlocalPort:%d\tremoteAppKey:%s\
                    \tremoteHost:%s\tremotePort:%d\tstart:%llu\ttype:%d\tstatus:%d\
                    \tcount:%d\tdebug:%d\tsample:%d\textend:%s\tcost:%u",\
                    pServerRpc->getTraceId().c_str(), pServerRpc->getRpcId().c_str(),\
                    pServerRpc->getRpcName().c_str(), pServerRpc->getLocalAppKey().c_str(),\
                    pServerRpc->getLocalHost().c_str(), pServerRpc->getLocalPort(),\
                    pServerRpc->getRemoteAppKey().c_str(), pServerRpc->getRemoteHost().c_str(),\
                    pServerRpc->getRemotePort(), pServerRpc->getStart(), pServerRpc->getType(),\
                    pServerRpc->getStatus(), pServerRpc->getCount(), pServerRpc->getDebug(),\
                    pServerRpc->getSample(), pServerRpc->getExtend().c_str(), pServerRpc->getCost());
            }
             
            //慢查询相关逻辑
            int cost = pServerRpc->getCost();
            string appKey = pServerRpc->getRemoteAppKey();
            string rpcName = pServerRpc->getRpcName();
            string appKeyAndName = appKey + "\t" + rpcName;

            pthread_rwlock_rdlock(&((TraceNameSpace::CONFIG).rwlock));
            map<string, int> sMap = (TraceNameSpace::CONFIG).identify2Limit; 
            map<string, int>::iterator iter = sMap.find(appKeyAndName);
            bool satisfy = (sMap.end() != iter || sMap.end() != (iter = sMap.find(appKey))) && (cost > iter->second);
            pthread_rwlock_unlock(&((TraceNameSpace::CONFIG).rwlock));
            
            if(satisfy)
            {
                CommonContentCollectClient<com::sankuai::cmtrace::TraceThresholdLog> * pCommClient = Singleton< CommonContentCollectClient<com::sankuai::cmtrace::TraceThresholdLog> >::getInstance();
                com::sankuai::cmtrace::TraceThresholdLog log;
                thresholdlogcpy(log, *pServerRpc);
                pCommClient->commonContentCollect(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST, log);

                COMM_DEBUG("[Trace::recvRpc]slowQuery:%d", cost);                     
            }
			
            TraceNameSpace::clearServerRpc(); 
		}
	}
	return 0;
}

int Trace::sendLog(const string &appKey, const string& content, int32_t level)
{
    if(!TraceNameSpace::m_init)
    {
        COMM_DEBUG("not Init before use");
        return -1;
    }
	uint64_t curTime = getCurrentMilliTime();
    ExceptionLog exceptionLog(appKey, content, level, curTime);
	Factory::getInstance()->getClient(LOG)->logCollect(exceptionLog);

    COMM_DEBUG("[Trace::sendLog]appKey:%s, content:%s, level:%d, time:%llu",\
                exceptionLog.getAppKey().c_str(), exceptionLog.getContent().c_str(),\
                exceptionLog.getLevel(), exceptionLog.getTime());
    return 0;
}
