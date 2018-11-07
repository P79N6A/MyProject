#include "CommandHandler.h"
#include "Handle.h"
#include "CommandExecutor.h"

namespace mafka
{


CommandHandler::CommandHandler()
:m_handle(new Handle())
{

}

CommandHandler::CommandHandler(HandlePool& pool)
:m_handle(pool.AllocHandlePtr())
{

}

CommandHandler::~CommandHandler()
{

}

void CommandHandler::Cancel()
{
	m_handle->Cancel();
}

bool CommandHandler::WaitRunning() {
	return true;
}

HandlePtr const& CommandHandler::GetHandle() const
{
	return m_handle;
}

void CommandHandler::SetExecutor(CommandExecutor* executor)
{
	m_pExecutor = executor;
}

CommandExecutor* CommandHandler::GetExecutor()
{
	return m_pExecutor;
}


}

