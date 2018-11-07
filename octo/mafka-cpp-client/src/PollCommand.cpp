#include "PollCommand.h"
#include "CommandExecutor.h"
#include "KafkaInstance.h"
#include "TimeUtil.h"

#include "Log.h"


namespace mafka
{

PollCommand::PollCommand(KafkaInstance& instance)
:m_instance(instance)
{
	m_Running = true;
}

void PollCommand::HandleCommand()
{
	//INFO("handleCommand run [%p]", m_instance);
	m_Running = true;
	CommandExecutor& executor = *(GetExecutor());
	{
		const int MAX_TIMEOUT = 100;
		timestamp_t now = TimeUtil::TimeNow();
		timestamp_t next_timer = executor.GetExceuteAt();
		int diff = int(next_timer - now);
		int timeout = executor.HasCommands() ? 5 : diff;
		if(timeout > MAX_TIMEOUT || timeout < 0)
		{
			timeout = MAX_TIMEOUT;
		}

		m_instance.PollEvent(timeout);
	}

	//INFO("handleCommand finish [%p]", m_instance);
	if(GetHandle()->IsCancelled()) {
		//INFO("handleCommand stop [%p]", m_instance);
		m_Running = false;
	} else {
		executor.Execute(*this);
	}
}

bool PollCommand::WaitRunning() {
	for(int i = 0; i < 10; i ++) {
		if(m_Running == false) {
			break;
		}
		usleep(100*1000);
	}
	
	return m_Running == false;
}


}

