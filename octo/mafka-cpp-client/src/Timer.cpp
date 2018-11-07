#include "Timer.h"
#include "CommandHandler.h"

namespace mafka
{


Timer::Timer(CommandHandler& handler, timestamp_t at, timestamp_t peroid)
:m_handler(handler), m_execute_at(at), m_exceute_period(peroid), m_handle(handler.GetHandle())
{
	m_handler.GetHandle()->Clear();
}

Timer::~Timer()
{

}

void Timer::Excecute(timestamp_t now)
{
	if(m_handle->IsCancelled())
		return;

	m_handler.HandleCommand();
	m_handle->Done();
	if(IsPeroid())
	{
		//m_execute_at += m_exceute_period;
		m_execute_at = now+m_exceute_period;
	}
}

timestamp_t Timer::GetExceuteAt() const
{
	return m_execute_at;
}

timestamp_t Timer::GetExceutePeroid() const
{
	return m_exceute_period;
}

HandlePtr const& Timer::GetHandle() const
{
	return m_handle;
}

bool Timer::IsPeroid() const
{
	return m_exceute_period > 0;
}

TimerPool::TimerPool(size_t threshold)
:Base(threshold)
{

}

TimerPool::~TimerPool()
{

}

Timer* TimerPool::AllocTimer(CommandHandler& handler, timestamp_t at, timestamp_t peroid)
{
	Timer* timer = Base::Alloc();
	return new (timer) Timer(handler, at, peroid);
}

void TimerPool::FreeTimer(Timer* timer)
{
	if(timer == NULL)
		return;

	timer->~Timer();
	Base::Free(timer);
}



}

