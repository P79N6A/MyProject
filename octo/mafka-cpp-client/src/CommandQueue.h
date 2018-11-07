#ifndef __MAFKA_COMMAND_QUEUE_H__
#define __MAFKA_COMMAND_QUEUE_H__

#include "Handle.h"
#include "Command.h"

#include <string>
#include <vector>
#include <queue>

namespace mafka
{

class Command;
class CommandPool;
class CommandHandler;
class Mutex;
class Condition;
class TimerQueue;

class CommandQueue
{
public:
	CommandQueue();
	virtual ~CommandQueue();

public:
	HandlePtr Execute(CommandHandler& handler, int priority = -1);

	bool HasCommands() const;
	size_t GetCommandCount() const;

public:
	void RunCommands(TimerQueue& timer_queue);
	void ClearCommands();

private:
	struct cmd_lessthan
	{
		bool operator()(Command* lhs, Command* rhs)
		{
			return lhs->GetPriority() < rhs->GetPriority();
		}
	};

	static const uint32_t COMMAND_QUEUE_CAPACITY = 1 << 18;
	CommandPool* m_pCommandPool;

	Mutex* m_pCommandMutex;
	Condition* m_pNotEmptyCondition;
	typedef std::vector<Command*> Commands;
	Commands m_foreground_cmdqueue;
	std::queue<Command*> m_background_cmdqueue;

};

}

#endif //__MAFKA_COMMAND_QUEUE_H__

