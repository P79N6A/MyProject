#ifndef _COMMONCOLLECT_H_
#define _COMMONCOLLECT_H_

#include "../cmtrace-gen-cpp/aggregator_common_constants.h"
#include "../cmtrace-gen-cpp/aggregator_common_types.h"
#include "../cmtrace-gen-cpp/SGAgent.h"
#include <boost/shared_ptr.hpp>
#include <transport/TTransportUtils.h>
#include "Singleton.h"
#include "Serialize.h"
#include "CommonTools.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

template<typename T>
class CommonContentCollectClient
{
public:
    static void * traceThresholdContentConsumer(void * args);
    //static void * mtraceContentConsumer(void * args);
    //static void * errorContentConsumer(void * args);
    
    /**
	* @Brief 收集日志信息
	* @param exceptionContent
	*/	
    void commonContentCollect(int logType, const T& commonContent);
    	    
    /**
	* @Brief 将已经序列化后的日志丢入消息队列
    * @param logType 日志类型
	* @param log 日志调用信息结构体
	* @return true表示插入成功
	*/
	bool putCommonContent(int logType, const T& commonContent);

	/**
	* @Brief 从消息队列中取出log调用信息
    * @param logType 日志类型
	* @return 取出的log信息
	*/
	T getCommonContent(int logType);

    /**
    * @Brief 获得队列大小
    * @return 
    */
    int getSize();
public:         
	TaskQueue<T> m_commonContentQueue;
private:
    friend class Singleton<CommonContentCollectClient>;
    CommonContentCollectClient();
    ~CommonContentCollectClient();
};

template<typename T>
CommonContentCollectClient<T>::CommonContentCollectClient():m_commonContentQueue(10000)
{
    //传递给线程的参数标识具体的日志类型，如MTRACE_LOG/ERROR_LOG/TRACE_THRESHOLD_LOG_LIST
    if(typeid(T) == typeid(com::sankuai::cmtrace::TraceThresholdLog))
    {
        pthread_t tidCommLog;
        void * param = (void *)&((com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST));
        pthread_create( &tidCommLog, NULL, traceThresholdContentConsumer, param);
    }
    else
    {
        //预留
    }
}

template<typename T>
CommonContentCollectClient<T>::~CommonContentCollectClient()
{

}

template<typename T>
void * CommonContentCollectClient<T>::traceThresholdContentConsumer(void * args)
{
    int type = *((int *)(args));

	if(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST == type)
    {
        CommonContentCollectClient<T> * pClient = Singleton< CommonContentCollectClient<T> >::getInstance();
        string SgAgentIp = getSgAgentIp();
        boost::shared_ptr<TSocket> pSocket(new TSocket(SgAgentIp, SgAgentPort));
        pSocket->setConnTimeout(100);
        pSocket->setSendTimeout(100);
        pSocket->setRecvTimeout(100);
        boost::shared_ptr<TFramedTransport> pFramedTransport(new TFramedTransport(pSocket));
        boost::shared_ptr<TBinaryProtocol> pProtocol(new TBinaryProtocol(pFramedTransport));
        com::sankuai::cmtrace::SGAgentClient client(pProtocol);
        vector<T> traceThresholdLogVec;

        while(1)
        {	
            int retrycount = 0;
            struct timeval start_tv;
            struct timeval current_tv;
            gettimeofday(&start_tv, NULL);
            long dis = 0;
            do
            {
                const T &traceThresholdLog = pClient->getCommonContent(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST);
                traceThresholdLogVec.push_back(traceThresholdLog);
                if(traceThresholdLogVec.size() >= SLOWQUERY_LIST_SIZE)
                {
                    break;
                }
                gettimeofday(&current_tv, NULL);
                dis = (current_tv.tv_sec - start_tv.tv_sec) * 1000 * 1000 + current_tv.tv_usec - start_tv.tv_usec;
            }while(dis < SLOWQUERY_SEND_GAP);

            com::sankuai::cmtrace::TraceThresholdLogList logList;
            logList.__set_logs(traceThresholdLogVec); 
            com::sankuai::cmtrace::CommonLog commonLog;
            commonLog.cmd = com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST;
            commonLog.content = ThriftToString(logList);  

            while(retrycount < 3)
            {
                try
                {
                    pFramedTransport->open(); ///< thrift 底层会判断当前socket是否可用，不可用再创建。                
                    client.uploadCommonLog(commonLog);
                    COMM_DEBUG("[traceThresholdContentConsumer]get SUCCESS, Ip:%s, remainder size:%d", SgAgentIp.c_str(), pClient->getSize());
                    break;
                }
                catch(TException &t)
                {
                    // warning log
                    COMM_DEBUG("[traceThresholdContentConsumer]WARNING:%s", t.what());
                    pFramedTransport->close();
                }
                retrycount++;
                usleep(SLEEPTIME); 
            }
            traceThresholdLogVec.clear();
        }
    }
    return (void *)0;
}

template<class T>
void CommonContentCollectClient<T>::commonContentCollect(int logType, const T& commonContent)
{
    if(SUCCESS == putCommonContent(logType, commonContent))
    {
        COMM_DEBUG("[commonContentCollect]put SUCCESS");
    }
    else
    {
        COMM_DEBUG("[commonContentCollect]put FAIL");
        //不再放入,以免递归
    }
}

template<class T>
bool CommonContentCollectClient<T>::putCommonContent(int logType, const T& commonContent)
{
    if(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST == logType)
    {
        return m_commonContentQueue.put(commonContent);
    }

    /* 理论上走不到 */
    return !SUCCESS;
}

template<class T>
T CommonContentCollectClient<T>::getCommonContent(int logType)
{
    if(com::sankuai::cmtrace::g_aggregator_common_constants.TRACE_THRESHOLD_LOG_LIST == logType)
    {
        return m_commonContentQueue.get();
    }
    else
    {
        //TODO
    }
        
    /* 理论上走不到 */
    T commContent;
    return commContent;
}

template<class T>
int CommonContentCollectClient<T>::getSize()
{
    return m_commonContentQueue.size();
}

#endif
