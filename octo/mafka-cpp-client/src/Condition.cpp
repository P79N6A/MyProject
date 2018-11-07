
#include "Condition.h"
#include "Mutex.h"
#include "TimeUtil.h"
#include "Log.h"

#include <errno.h>
#include <stdio.h>

namespace mafka
{

Condition::Condition()
{
	int err = pthread_cond_init(&m_cond, NULL);
	if(err)
	{
		ERROR("*****pthread_cond_init err=%d\n", err);
	}
}

Condition::~Condition()
{
	int err = pthread_cond_destroy(&m_cond);
	if(err)
	{
		ERROR("*****pthread_cond_destroy err=%d\n", err);
	}
}

void Condition::Signal()
{
	int err = pthread_cond_signal(&m_cond);
	if(err)
	{
		ERROR("*****pthread_cond_signal err=%d\n", err);
	}
}

void Condition::Broadcast()
{
	int err = pthread_cond_broadcast(&m_cond);
	if(err)
	{
		ERROR("*****pthread_cond_broadcast err=%d\n", err);
	}
}

void Condition::Wait(Mutex& mutex)
{
	int err = pthread_cond_wait(&m_cond, &mutex.m_mutex);
	if(err)
	{
		ERROR("*****pthread_cond_wait err=%d\n", err);
	}
}

bool Condition::TimedWait(Mutex& mutex, timestamp_t timeout)
{
	struct timespec ts;
	TimeUtil::TimestampToTimespec(TimeUtil::TimeNow()+timeout, &ts);

	int err = pthread_cond_timedwait(&m_cond, &mutex.m_mutex, &ts);
	if(err && err != ETIMEDOUT)
	{
		ERROR("*****pthread_cond_timedwait err=%d\n", err);
	}

	return err == ETIMEDOUT;
}

}

