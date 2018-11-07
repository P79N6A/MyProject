#ifndef __MAFKA_POLL_COMMAND_H__
#define __MAFKA_POLL_COMMAND_H__

#include "CommandHandler.h"

namespace mafka
{

class KafkaInstance;
class PollCommand : public CommandHandler
{
public:
	PollCommand(KafkaInstance& instance);
	virtual void HandleCommand();

	bool WaitRunning();
private:
	KafkaInstance& m_instance;
	bool m_Running;
};



}



#endif //__MAFKA_POLL_COMMAND_H__
