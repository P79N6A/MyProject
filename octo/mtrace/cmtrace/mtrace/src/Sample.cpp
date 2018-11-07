#include <pthread.h>
#include <map>
#include <stdio.h>
#include <time.h>
#include "Sample.h"
#include "CommonTools.h"
#include "MTrace.h"
using namespace std;

Sample * Sample::m_instance = NULL;
typedef map<string, SampleStat> SMap;
typedef map<string, SampleStat>::iterator Iter;
typedef pair<map<string, SampleStat>::iterator, bool> Pair;

static pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;
static const int g_minSampleTimeGap = 10; //最小采样时间时间10s
static const int g_maxSampleTimeGap = 20; //最大采样时间间隔20s
static const int g_sampleCountThreshold = 1000; //一分钟的采样阈为1000次
static const int g_sampleTimeStep = 1; //1s
static const int g_sMapKeyCount = 2048; //为了控制内存，限制map中存储的最多的key的个数
static SMap g_sMap;

static const string generate_key(const RemoteProcessCall& rpc)
{	
	int32_t type = rpc.getType();
	const string remoteAppKey = rpc.getRemoteAppKey();
	const string remoteHost = rpc.getRemoteHost();
	const string rpcName = rpc.getRpcName(); 
	char buf[1024] = {0};
	snprintf(buf, sizeof(buf), "%d\t%s\t%s\t%s", type, remoteAppKey.c_str(), remoteHost.c_str(), rpcName.c_str());
	return string(buf);
}

Sample::Sample():m_lastMinute(0), m_sampleSum(0)
{
	m_sampleTimeGap = g_minSampleTimeGap;
}

Sample * Sample::getInstance()
{
	if(NULL == m_instance)
	{
		pthread_mutex_lock(&g_mutex);
		if(NULL == m_instance)
		{
			m_instance = new Sample;
		}
		pthread_mutex_unlock(&g_mutex);
	}
	return m_instance;
}

bool Sample::judgeSample(RemoteProcessCall& rpc)
{
	const string sampleKey = generate_key(rpc);
	uint64_t curSecondTime = time(0);

	pthread_mutex_lock(&g_mutex);   //Client和Server互斥
	Iter iter = g_sMap.find(sampleKey);
	if(rpc.getSample())
	{
        if(g_sMap.end() != iter) //上游采样
		{
			iter->second.m_count++;
            rpc.setCount(iter->second.m_count);
			            
            iter->second.m_count = 0; // 复位count
            iter->second.m_lastSampleTime = curSecondTime;
		}
        else //全采样调试
        {
            rpc.setCount(1);
        }
	}
	else
	{	
		if(g_sMap.end() == iter)
		{
			/// 这里只是为了以防内存无限制增大，特殊情况下才会用到
			if(g_sMap.size() >= g_sMapKeyCount)
			{
				Iter begIter = g_sMap.begin();
				g_sMap.erase(begIter);
                /*
				string content = "ip:" + TraceNameSpace::m_localIp + "|msg:sample Map is Full";
				int32_t level = 1;
				Trace::getInstance()->sendLog(rpc.getLocalAppKey(), content,level);
                COMM_DEBUG("[Sample::judgeSample]ERROR:%s", content.c_str());*/
			}

			struct SampleStat sampleStat;
			sampleStat.m_lastSampleTime = curSecondTime;
			sampleStat.m_count = 0;
			Pair insertPair = g_sMap.insert(make_pair(sampleKey, sampleStat));				
			if(insertPair.second) 
			{
				iter = insertPair.first;
			}
		}
		
		if(g_sMap.end() != iter) //如果上面插入失败，这个迭代器有可能依然指向end
		{
			iter->second.m_count++; 
			if(curSecondTime - iter->second.m_lastSampleTime >= m_sampleTimeGap)
			{
				rpc.setSample(true);
				rpc.setCount(iter->second.m_count);
			
                COMM_DEBUG("[Sample::judgeSample]sample:%d\tsampleKey:%s\tm_lastSampleTime:%llu\tm_count:%d\
                        \tm_lastMinute:%llu\tm_sampleSum:%d\tm_sampleTimeGap:%d",\
                        rpc.getSample(), sampleKey.c_str(), iter->second.m_lastSampleTime,\
                        iter->second.m_count, m_lastMinute, m_sampleSum, m_sampleTimeGap);
					
				iter->second.m_count = 0; // 复位count
				iter->second.m_lastSampleTime = curSecondTime;
				
				updateSampleGap(curSecondTime);
			}
			else
			{
				rpc.setSample(false);
                COMM_DEBUG("[Sample::judgeSample]sample:%d\tsampleKey:%s\tm_lastSampleTime:%llu\tm_count:%d\
                        \tm_lastMinute:%llu\tm_sampleSum:%d\tm_sampleTimeGap:%d",\
                        rpc.getSample(), sampleKey.c_str(), iter->second.m_lastSampleTime,\
                        iter->second.m_count, m_lastMinute, m_sampleSum, m_sampleTimeGap);	
			}
			
        }
	}
	pthread_mutex_unlock(&g_mutex);
	return rpc.getSample();
}

void Sample::updateSampleGap(uint64_t curSecondTime)
{
	uint64_t curMinute = curSecondTime/60;
	if(m_lastMinute != curMinute)
	{
		if(m_sampleSum <= g_sampleCountThreshold && m_sampleTimeGap > g_minSampleTimeGap) //采样次数过少，需要减少时间间隔
		{
			m_sampleTimeGap = m_sampleTimeGap - g_sampleTimeStep;
		}
		else if(m_sampleSum > g_sampleCountThreshold && m_sampleTimeGap < g_maxSampleTimeGap) //采样次数过多，需要增加时间间隔
		{
			m_sampleTimeGap = m_sampleTimeGap + g_sampleTimeStep;
		}
		
		if(m_sampleTimeGap < g_minSampleTimeGap)
		{
			m_sampleTimeGap = g_minSampleTimeGap;
		}
		else if(m_sampleTimeGap > g_maxSampleTimeGap)
		{
			m_sampleTimeGap = g_maxSampleTimeGap;
		}

		m_lastMinute = curMinute;
		m_sampleSum = 1;
	}
	else
	{
		m_sampleSum++;
	}
	return;
}
