/**
* @file Sample.h
* @Brief 采样类
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-28
*/
#ifndef __SAMPLE_H__
#define __SAMPLE_H__

#include "RemoteProcessCall.h"

struct SampleStat
{
	int m_lastSampleTime; ///< 上次抽样时间
	int m_count; ///< 上次抽样到现在的调用次数统计 
};

class Sample
{
public:
	static Sample * getInstance();
	
	/**
	* @Brief 判断是否采样，并更新rpc中的sample
	* @param rpc
	* @return 
	*/
	bool judgeSample(RemoteProcessCall& rpc);
private:
	Sample();
	static Sample * m_instance;
	
	/**
	* @Brief 更新采样时间间隔，时间控制在g_minSampleTimeGap 和 g_maxSampleTimeGap 之间
	* @param curSecondTime
	*/
	inline void updateSampleGap(uint64_t curSecondTime);

	int m_sampleTimeGap; ///< 采样时间间隔

	uint64_t m_lastMinute; ///<上一分钟
	int m_sampleSum; ///<上一分钟采样总数
};

#endif
