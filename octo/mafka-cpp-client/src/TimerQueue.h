#ifndef __MAFKA_TIMER_QUEUE_H__
#define __MAFKA_TIMER_QUEUE_H__

#include "Handle.h"
#include "Timer.h"

#include <string>
#include <vector>
#include <queue>

namespace mafka
{

class Timer;
class TimerPool;
class CommandHandler;
class Thread;
class Mutex;
class Condition;

class TimerQueue
{
public:
	TimerQueue();
	virtual ~TimerQueue();

public:
	HandlePtr ExecuteAfter(CommandHandler& handler, timestamp_t after_time/*relative time*/);
	HandlePtr ExecuteEvery(CommandHandler& handler, timestamp_t peroid);

	bool HasTimers() const;
	size_t GetTimerCount() const;
	timestamp_t GetExceuteAt();

public:
	void RunTimers();
	void ClearTimers();

private:
	struct timer_greaterthan
	{
		bool operator()(Timer* lhs, Timer* rhs)
		{
			return lhs->GetExceuteAt() > rhs->GetExceuteAt();
		}
	};

	static const uint32_t TIMER_QUEUE_CAPACITY = 1 << 11;
	TimerPool* m_pTimerPool;
	Mutex* m_pTimersMutex;
	typedef std::vector<Timer*> Timers;
	Timers m_foreground_timerqueue;
	std::priority_queue<Timer*, Timers, timer_greaterthan> m_background_timerqueue;
};

}

#endif //__MAFKA_TIMER_QUEUE_H__

