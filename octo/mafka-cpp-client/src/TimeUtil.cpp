#include "TimeUtil.h"

#include <unistd.h>
#include <sys/time.h>

namespace mafka
{


timestamp_t TimeUtil::TimeNow()
{
	static timestamp_t INVALID_TIME = 0;

	struct timeval now;
	if (gettimeofday(&now, NULL))
		return INVALID_TIME;
	return ((timestamp_t)now.tv_sec) * 1000 + (now.tv_usec / 1000);
}

timestamp_t TimeUtil::TimeNowMicroSeconds()
{
	static timestamp_t INVALID_TIME = 0;

	struct timeval now;
	if (gettimeofday(&now, NULL))
		return INVALID_TIME;
	return ((timestamp_t)now.tv_sec) * 1000 * 1000 + now.tv_usec;
}

void TimeUtil::TimestampToTimespec(timestamp_t t, struct timespec* ts)
{
	if(ts)
	{
		ts->tv_sec = t / 1000;
		ts->tv_nsec = (t % 1000) * 1000 * 1000;
	}
}



}

