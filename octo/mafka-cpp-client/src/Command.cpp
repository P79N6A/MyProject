#include "Command.h"
#include "CommandHandler.h"

namespace mafka
{


Command::Command(CommandHandler& handler, int priority)
:m_handler(handler), m_priority(priority), m_handle(handler.GetHandle())
{
	m_handler.GetHandle()->Clear();
}

Command::~Command()
{

}

void Command::Excecute()
{
	if(m_handle->IsCancelled())
		return;

	m_handler.HandleCommand();
	m_handle->Done();	
}

HandlePtr const& Command::GetHandle() const
{
	return m_handle;
}

int Command::GetPriority() const
{
	return m_priority;
}

CommandPool::CommandPool(size_t threshold)
:Base(threshold)
{

}

CommandPool::~CommandPool()
{

}

Command* CommandPool::AllocCommand(CommandHandler& handler, int priority)
{
	Command* cmd = Base::Alloc();
	return new (cmd) Command(handler, priority);
}

void CommandPool::FreeCommand(Command* cmd)
{
	if(cmd == NULL)
		return;

	cmd->~Command();
	Base::Free(cmd);
}


}
