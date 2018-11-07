#include "TimerQueue.h"
#include "Timer.h"
#include "CommandHandler.h"
#include "Thread.h"
#include "Mutex.h"
#include "Condition.h"
#include "LockGuard.h"
#include "TimeUtil.h"
#include "Log.h"

#include <sched.h>


namespace mafka
{

TimerQueue::TimerQueue()
:m_pTimerPool(new TimerPool(TIMER_QUEUE_CAPACITY))
,m_pTimersMutex(new Mutex())
{
	m_foreground_timerqueue.reserve(TIMER_QUEUE_CAPACITY);
}

TimerQueue::~TimerQueue()
{
	delete m_pTimerPool;
	delete m_pTimersMutex;
}

HandlePtr TimerQueue::ExecuteAfter(CommandHandler& handler, timestamp_t after_time/*relative time*/)
{
	//Timer* timer = new Timer(handler, time_now()+after_time, 0);
	Timer* timer = m_pTimerPool->AllocTimer(handler, TimeUtil::TimeNow()+after_time, 0);
	HandlePtr handle = timer->GetHandle();
	if(handle->IsCancelled())
	{
		ERROR("BUG::TimerQueue::ExecuteAfter bug found, timer=%p\n", timer);
	}

	{
		LockGuard guard(*m_pTimersMutex);
		m_foreground_timerqueue.push_back(timer);
	}

	return handle;
}

HandlePtr TimerQueue::ExecuteEvery(CommandHandler& handler, timestamp_t peroid)
{
	//Timer* timer = new Timer(handler, time_now()+peroid, peroid);
	Timer* timer = m_pTimerPool->AllocTimer(handler, TimeUtil::TimeNow()+peroid, peroid);
	HandlePtr handle = timer->GetHandle();
	if(handle->IsCancelled())
	{
		ERROR("BUG::TimerQueue::ExecuteEvery bug found, timer=%p\n", timer);
	}

	{
		LockGuard guard(*m_pTimersMutex);
		m_foreground_timerqueue.push_back(timer);
	}

	return handle;
}

bool TimerQueue::HasTimers() const
{
	LockGuard guard(*m_pTimersMutex);
	return !m_foreground_timerqueue.empty() || !m_background_timerqueue.empty();
}

size_t TimerQueue::GetTimerCount() const
{
	LockGuard guard(*m_pTimersMutex);
	return m_foreground_timerqueue.size() + m_background_timerqueue.size();
}

timestamp_t TimerQueue::GetExceuteAt()
{
	if(m_background_timerqueue.empty())
		return MAX_ABS_TIME;

	Timer* timer = m_background_timerqueue.top();
	return timer->GetExceuteAt();
}

void TimerQueue::RunTimers()
{
	{
		LockGuard guard(*m_pTimersMutex);
		for(Timers::iterator it = m_foreground_timerqueue.begin(); it != m_foreground_timerqueue.end(); ++it)
		{
			m_background_timerqueue.push(*it);
		}
		m_foreground_timerqueue.clear();
	}

	if(!m_background_timerqueue.empty())
	{
		timestamp_t now = TimeUtil::TimeNow();
		while (!m_background_timerqueue.empty())
		{
			Timer* timer = m_background_timerqueue.top();
			if(timer->GetExceuteAt() > now)
			{
				break;
			}
			m_background_timerqueue.pop();

			timer->Excecute(now);

			if(timer->IsPeroid() && !timer->GetHandle()->IsCancelled())
			{
				m_background_timerqueue.push(timer);
			}
			else
			{
				m_pTimerPool->FreeTimer(timer);
			}
		}
	}

}

void TimerQueue::ClearTimers()
{
	{
		LockGuard guard(*m_pTimersMutex);
		for(Timers::iterator it = m_foreground_timerqueue.begin(); it != m_foreground_timerqueue.end(); ++it)
		{
			m_background_timerqueue.push(*it);
		}
		m_foreground_timerqueue.clear();
	}

	while (!m_background_timerqueue.empty())
	{
		Timer* timer = m_background_timerqueue.top();
		m_background_timerqueue.pop();
		m_pTimerPool->FreeTimer(timer);
	}
}


}

