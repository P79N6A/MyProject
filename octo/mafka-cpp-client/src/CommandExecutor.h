#ifndef __MAFKA_COMMAND_EXECUTOR_H__
#define __MAFKA_COMMAND_EXECUTOR_H__

#include "Runable.h"
#include "Handle.h"

#include <string>

namespace mafka
{

class CommandHandler;
class Thread;
class CommandQueue;
class TimerQueue;
class CommandExecutor : public Runable
{
public:
	CommandExecutor();
	CommandExecutor(std::string const& name);
	virtual ~CommandExecutor();

public:
	void Start();
	void Stop();
	void WaitDone();

	std::string const& GetName() const;

	//static CommandExecutor& Current();

	HandlePtr Execute(CommandHandler& handler, int priority = -1);
	HandlePtr ExecuteAfter(CommandHandler& handler, timestamp_t after_time/*relative time*/);
	HandlePtr ExecuteEvery(CommandHandler& handler, timestamp_t peroid);

	virtual void Run();

	bool HasCommands() const;
	size_t GetCommandCount() const;

	bool HasTimers() const;
	size_t GetTimerCount() const;
	timestamp_t GetExceuteAt();

private:
	Thread* m_pThread;
	bool m_bStop;

	CommandQueue* m_pCommandQueue;
	TimerQueue* m_pTimerQueue;
};

}

#endif //__MAFKA_COMMAND_EXECUTOR_H__

