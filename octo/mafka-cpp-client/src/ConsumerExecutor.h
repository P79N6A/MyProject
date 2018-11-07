#ifndef __MAFKA_CONSUMER_EXECUTOR_H__
#define __MAFKA_CONSUMER_EXECUTOR_H__

#include <string>
#include <vector>

namespace mafka
{

class MessageHandler;
class HandlerProto
{
public:
	HandlerProto(MessageHandler& handler);
	MessageHandler& GetHandler() const;

private:
	MessageHandler& m_handler;
};

class CommandExecutor;
class ConsumeCommand;
class ConsumerImpl;
class ConsumerExecutor
{
public:
	ConsumerExecutor(std::string const& name);
	virtual ~ConsumerExecutor();

public:
	void Startup();
	void Shutdown();

	void Consume(ConsumerImpl& consumer, HandlerProto& proto, int partition);
	bool Reset(ConsumerImpl& consumer);

	void CancelConsume();

	void StopExecutor();

private:
	CommandExecutor* m_pExecutor;

	typedef std::vector<ConsumeCommand*> ConsumeCommands;
	ConsumeCommands m_consume_commands;
};



}



#endif //__MAFKA_CONSUMER_EXECUTOR_H__
