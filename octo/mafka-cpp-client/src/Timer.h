#ifndef __MAFKA_TIMER_H__
#define __MAFKA_TIMER_H__

#include "Common.h"
#include "Handle.h"
#include "ObjectPool.h"

namespace mafka
{

class CommandHandler;
class Timer
{
public:
	Timer(CommandHandler& handler, timestamp_t at, timestamp_t peroid);
	~Timer();

public:
	void Excecute(timestamp_t now);

	timestamp_t GetExceuteAt() const;
	timestamp_t GetExceutePeroid() const;

	HandlePtr const& GetHandle() const;

	bool IsPeroid() const;

private:
	CommandHandler& m_handler;

	timestamp_t m_execute_at;
	timestamp_t m_exceute_period;
	HandlePtr m_handle;
};

class TimerPool : private ObjectPool<Timer>
{
public:
	typedef ObjectPool<Timer> Base;

public:
	TimerPool(size_t threshold);
	~TimerPool();

public:
	Timer* AllocTimer(CommandHandler& handler, timestamp_t at, timestamp_t peroid);
	void FreeTimer(Timer* timer);
};


}


#endif //__NMQ_TIMER_H__
