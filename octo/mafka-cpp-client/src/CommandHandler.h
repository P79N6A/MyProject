#ifndef __MAFKA_COMMAND_HANDLER_H__
#define __MAFKA_COMMAND_HANDLER_H__

#include "Handle.h"

namespace mafka
{

class CommandExecutor;
class CommandHandler
{
public:
	CommandHandler();
	CommandHandler(HandlePool& pool);

	virtual ~CommandHandler();

public:
	virtual void HandleCommand() = 0;
	virtual void Cancel();
	
	bool WaitRunning();
	void SetExecutor(CommandExecutor* executor);
	CommandExecutor* GetExecutor();

public:
	HandlePtr const& GetHandle() const;

private:
	HandlePtr m_handle;
        CommandExecutor* m_pExecutor;
};

}


#endif //__MAFKA_COMMAND_HANDLER_H__
