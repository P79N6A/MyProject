#ifndef __MAFKA_COMMAND_H__
#define __MAFKA_COMMAND_H__

#include "Handle.h"
#include "ObjectPool.h"

namespace mafka
{

class CommandHandler;
class Command
{
public:
	Command(CommandHandler& handler, int priority);
	~Command();

public:
	void Excecute();

	HandlePtr const& GetHandle() const;
	int GetPriority() const;

private:
	CommandHandler& m_handler;
	int m_priority;
	HandlePtr m_handle;
};

class CommandPool : private ObjectPool<Command>
{
public:
	typedef ObjectPool<Command> Base;

public:
	CommandPool(size_t threshold);
	~CommandPool();

public:
	Command* AllocCommand(CommandHandler& handler, int priority);
	void FreeCommand(Command* cmd);
};

}


#endif //__MAFKA_COMMAND_H__