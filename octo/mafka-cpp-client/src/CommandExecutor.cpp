#include "CommandExecutor.h"
#include "Thread.h"
#include "CommandQueue.h"
#include "TimerQueue.h"
#include "TimeUtil.h"
#include "Log.h"
#include "CommandHandler.h"
#include <sched.h>


namespace mafka
{

CommandExecutor::CommandExecutor()
:m_pThread(new MainThread(*this)), m_bStop(false)
,m_pCommandQueue(new CommandQueue())
,m_pTimerQueue(new TimerQueue())
{
	m_pThread->SetUserData(this);
}

CommandExecutor::CommandExecutor(std::string const& name)
:m_pThread(new Thread(name, *this)), m_bStop(true)
,m_pCommandQueue(new CommandQueue())
,m_pTimerQueue(new TimerQueue())
{
	m_pThread->SetUserData(this);
}

CommandExecutor::~CommandExecutor()
{
	delete m_pThread;

	delete m_pCommandQueue;
	delete m_pTimerQueue;
}

void CommandExecutor::Start()
{
	if(!m_bStop)
		return;

	m_bStop = false;
	m_pThread->Start();
}

void CommandExecutor::Stop()
{
	m_bStop = true;
}

void CommandExecutor::WaitDone()
{
	m_pThread->WaitDone();
}

std::string const& CommandExecutor::GetName() const
{
	return m_pThread->GetName();
}
/*
CommandExecutor& CommandExecutor::Current()
{
	return *(static_cast<CommandExecutor*>(Thread::Current().GetUserData()));
}
*/
HandlePtr CommandExecutor::Execute(CommandHandler& handler, int priority/* = -1*/)
{
	handler.SetExecutor(this);
	return m_pCommandQueue->Execute(handler, priority);
}

HandlePtr CommandExecutor::ExecuteAfter(CommandHandler& handler, timestamp_t after_time/*relative time*/)
{
	handler.SetExecutor(this);
	return m_pTimerQueue->ExecuteAfter(handler, after_time);
}

HandlePtr CommandExecutor::ExecuteEvery(CommandHandler& handler, timestamp_t peroid)
{
	handler.SetExecutor(this);
	return m_pTimerQueue->ExecuteEvery(handler, peroid);
}

void CommandExecutor::Run()
{
	while(!m_bStop)
	{
		//process timers
		m_pTimerQueue->RunTimers();
		//process commands
		m_pCommandQueue->RunCommands(*m_pTimerQueue);
	}

	m_pTimerQueue->ClearTimers();
	m_pCommandQueue->ClearCommands();
}

bool CommandExecutor::HasCommands() const
{
	return m_pCommandQueue->HasCommands();
}

size_t CommandExecutor::GetCommandCount() const
{
	return m_pCommandQueue->GetCommandCount();
}

bool CommandExecutor::HasTimers() const
{
	return m_pTimerQueue->HasTimers();
}

size_t CommandExecutor::GetTimerCount() const
{
	return m_pTimerQueue->GetTimerCount();
}

timestamp_t CommandExecutor::GetExceuteAt()
{
	return m_pTimerQueue->GetExceuteAt();
}


}

