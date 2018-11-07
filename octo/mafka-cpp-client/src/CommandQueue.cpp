#include "CommandQueue.h"
#include "TimerQueue.h"
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

CommandQueue::CommandQueue()
:m_pCommandPool(new CommandPool(COMMAND_QUEUE_CAPACITY))
,m_pCommandMutex(new Mutex()), m_pNotEmptyCondition(new Condition())
{
	m_foreground_cmdqueue.reserve(COMMAND_QUEUE_CAPACITY);
}

CommandQueue::~CommandQueue()
{
	delete m_pCommandPool;
	delete m_pCommandMutex;
	delete m_pNotEmptyCondition;
}

HandlePtr CommandQueue::Execute(CommandHandler& handler, int priority/* = -1*/)
{
	//Command* cmd = new Command(handler, priority);
	Command* cmd = m_pCommandPool->AllocCommand(handler, priority);
	HandlePtr handle = cmd->GetHandle();
	if(handle->IsCancelled())
	{
		ERROR("BUG::CommandQueue::Execute bug found, cmd=%p\n", cmd);
	}

	bool empty = false;
	{
		LockGuard guard(*m_pCommandMutex);
		empty = m_foreground_cmdqueue.empty();
		m_foreground_cmdqueue.push_back(cmd);
	}
	if(empty)
	{
		m_pNotEmptyCondition->Signal();
	}

	return handle;
}

bool CommandQueue::HasCommands() const
{
	LockGuard guard(*m_pCommandMutex);
	return !m_foreground_cmdqueue.empty() || !m_background_cmdqueue.empty();
}

size_t CommandQueue::GetCommandCount() const
{
	LockGuard guard(*m_pCommandMutex);
	return m_foreground_cmdqueue.size() + m_background_cmdqueue.size();
}

void CommandQueue::RunCommands(TimerQueue& timer_queue)
{
	//swap foreground and background command queue
	{
		LockGuard guard(*m_pCommandMutex);
		if(m_foreground_cmdqueue.empty())
		{
			const timestamp_t DEFAULT_WAITTIME = 10;
			const timestamp_t MAX_WAITTIME = 30;
			timestamp_t wait_time = DEFAULT_WAITTIME;
			wait_time = timer_queue.GetExceuteAt() - TimeUtil::TimeNow();
			if(wait_time <= 0)
				wait_time = DEFAULT_WAITTIME;
			if(wait_time > MAX_WAITTIME)
				wait_time = MAX_WAITTIME;
			m_pNotEmptyCondition->TimedWait(*m_pCommandMutex, wait_time);
		}
		for(Commands::iterator it = m_foreground_cmdqueue.begin(); it != m_foreground_cmdqueue.end(); ++it)
		{
			m_background_cmdqueue.push(*it);
		}
		m_foreground_cmdqueue.clear();
	}

	Command* cmd = NULL;
	while (!m_background_cmdqueue.empty())
	{
		cmd = m_background_cmdqueue.front();
		m_background_cmdqueue.pop();
		cmd->Excecute();

		//delete cmd;
		m_pCommandPool->FreeCommand(cmd);
	}

}

void CommandQueue::ClearCommands()
{
	//swap foreground and background command queue
	{
		LockGuard guard(*m_pCommandMutex);
		if(m_foreground_cmdqueue.empty())
		{
			return;
		}

		for(Commands::iterator it = m_foreground_cmdqueue.begin(); it != m_foreground_cmdqueue.end(); ++it)
		{
			m_background_cmdqueue.push(*it);
		}
		m_foreground_cmdqueue.clear();
	}

	Command* cmd = NULL;
	while (!m_background_cmdqueue.empty())
	{
		cmd = m_background_cmdqueue.front();
		m_background_cmdqueue.pop();
		// all done
		cmd->Excecute();
		//delete cmd;
		m_pCommandPool->FreeCommand(cmd);
	}

}



}

