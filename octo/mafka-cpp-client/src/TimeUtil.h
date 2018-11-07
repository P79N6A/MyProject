#ifndef __MAFKA_TIME_UTIL_H__
#define __MAFKA_TIME_UTIL_H__

#include <string>
#include <map>

#include "Common.h"

namespace mafka
{


class TimeUtil
{
public:
	static timestamp_t TimeNow();
	static timestamp_t TimeNowMicroSeconds();
	
	static void TimestampToTimespec(timestamp_t t, struct timespec* ts);

};


}


#endif //__MAFKA_TIME_UTIL_H__

