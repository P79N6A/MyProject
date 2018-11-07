#include "ConsumerExecutor.h"
#include "CommandHandler.h"
#include "MessageHandler.h"

#include "ConsumerImpl.h"

#include "CommandExecutor.h"
#include "CommandHandler.h"
#include "KafkaTopic.h"
#include "KafkaInstance.h"
#include "StringDecoder.h"


namespace mafka
{

HandlerProto::HandlerProto(MessageHandler& handler)
:m_handler(handler)
{

}

MessageHandler& HandlerProto::GetHandler() const
{
	return m_handler;
}

class ConsumeCommand : public CommandHandler
{
public:
	ConsumeCommand(ConsumerImpl& consumer, HandlerProto const& proto, int partition);
	virtual void HandleCommand();

	int GetPartition() const;

private:
	ConsumerImpl& m_consumer;
	MessageHandler& m_handler;
	rd_kafka_message_t* m_cur_message;
	int m_partition;
	int m_retry_count;
};

ConsumeCommand::ConsumeCommand(ConsumerImpl& consumer, HandlerProto const& proto, int partition)
:m_consumer(consumer), m_handler(proto.GetHandler()), m_cur_message(NULL), m_partition(partition), m_retry_count(0)
{

}

void ConsumeCommand::HandleCommand()
{
	ConsumeStatus status = CONSUME_SUCCESS;
	char* dstContent = NULL;
	int len = 0;
	
	if(m_cur_message == NULL)
	{
		m_cur_message = rd_kafka_consume(m_consumer.m_pKafkaTopic->GetHandle(), m_partition, 1);
		DEBUG("got message=%p, partition=%d, error=%d", m_cur_message, m_partition, rd_kafka_last_error());
		if (m_cur_message == NULL) /* timeout */
		{
			goto out;
		}
	}

	if(m_cur_message->err)
	{
		ERROR("rd_kafka_consume message failed, partition=%d, errcode=%d, errorstr=%s, offset=%llu\n",
			   	m_cur_message->partition, m_cur_message->err, (char*)m_cur_message->payload, m_cur_message->offset);
		goto out;
	}

    
	StringDecoder::stringDecode(static_cast<const char*>(m_cur_message->payload), dstContent, len);	

	INFO("message partition:[%d] messageContent:[%s] len:[%d]", m_partition, dstContent, len);
	
	status = m_handler.OnRecvMessage(m_cur_message->partition, dstContent, len);
	if(status == CONSUME_LATER){
		if(m_retry_count++ >= RETRY_COUNT_MAX){
			WARN("parition=%d, offset=%llu, retry %d times, drop", m_cur_message->partition, m_cur_message->offset, RETRY_COUNT_MAX);
		}
	}
	//wait 10ms, to prevent retry too frequently
	if(status != CONSUME_SUCCESS)
	{
		usleep(10 * 1000);
	}
out:
	/* Return message to rdkafka */
	if(status == CONSUME_SUCCESS || (status == CONSUME_LATER && m_retry_count > RETRY_COUNT_MAX))
	{
		if(m_cur_message != NULL)
		{
		     	rd_kafka_t* rd_kafka_t = m_consumer.m_pKafkaInstance->GetHandle();	
			INFO("commit message [%p], parition=%d, offset=%llu, status [%d]\n", rd_kafka_t, m_cur_message->partition, m_cur_message->offset, status);
			if(!m_cur_message->err){
				rd_kafka_offset_store(m_cur_message->rkt, m_cur_message->partition, m_cur_message->offset);
			}
			rd_kafka_commit_message(rd_kafka_t, m_cur_message, 1);
			rd_kafka_message_destroy(m_cur_message);
			m_cur_message = NULL;
		}
		m_retry_count = 0;
	}
	if(!GetHandle()->IsCancelled()){
		GetExecutor()->Execute(*this);
	}else{
		WARN("Command cancelled, do not need excute again");
	}
}

int ConsumeCommand::GetPartition() const
{
	return m_partition;
}


ConsumerExecutor::ConsumerExecutor(std::string const& name)
:m_pExecutor(new CommandExecutor(name))
{

}

ConsumerExecutor::~ConsumerExecutor()
{
	delete m_pExecutor;
}

void ConsumerExecutor::Startup()
{
	m_pExecutor->Start();
}

void ConsumerExecutor::StopExecutor() {
	
	m_pExecutor->Stop();
	m_pExecutor->WaitDone();
}

void ConsumerExecutor::Shutdown()
{

	for(ConsumeCommands::iterator i = m_consume_commands.begin(); i != m_consume_commands.end(); ++i)
	{
		ConsumeCommand* consume_command = *i;
		delete consume_command;
	}
}

void ConsumerExecutor::Consume(ConsumerImpl& consumer, HandlerProto& proto, int partition)
{
	INFO("consume partition:%d", partition);
    	

	if (rd_kafka_consume_start(consumer.m_pKafkaTopic->GetHandle(), partition, RD_KAFKA_OFFSET_STORED) == -1)
	{
		ERROR("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] startup failed, err=%d",
			consumer.GetAppKey().c_str(), consumer.GetTopic().c_str(), consumer.GetConsumerGroup().c_str(), partition, rd_kafka_last_error());
	}
    consumer.m_pKafkaTopic->updatePartitionStatusMap(partition, START_OK);	
	INFO("update status ok handle:[%p], partition:[%d]\n",
				consumer.m_pKafkaTopic->GetHandle(), partition);
	ConsumeCommand* consume_command = new ConsumeCommand(consumer, proto, partition);
	m_pExecutor->Execute(*consume_command);
	m_consume_commands.push_back(consume_command);
}

void ConsumerExecutor::CancelConsume() {
	
	for(ConsumeCommands::iterator i = m_consume_commands.begin(); i != m_consume_commands.end(); ++i)
	{
		ConsumeCommand* consume_command = *i;
		consume_command->Cancel();
    }
}

bool ConsumerExecutor::Reset(ConsumerImpl& consumer)
{
	for(ConsumeCommands::iterator i = m_consume_commands.begin(); i != m_consume_commands.end(); ++i)
	{
		ConsumeCommand* consume_command = *i;
		int partition = consume_command->GetPartition();
		INFO("remove partition:%d", partition);
		bool bRunStart = false;
		if( consumer.m_pKafkaTopic->existPartitionId(partition) == false) {
			bRunStart = true;
		} else {
			if ( consumer.m_pKafkaTopic->getPartitionStatus(partition) != START_OK ) {
				bRunStart = true;
			}
		}
		
		if(bRunStart) {
			if (rd_kafka_consume_start(consumer.m_pKafkaTopic->GetHandle(), partition, RD_KAFKA_OFFSET_STORED) == -1)
			{
				ERROR("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] startup failed, err=%d",
					consumer.GetAppKey().c_str(), consumer.GetTopic().c_str(), consumer.GetConsumerGroup().c_str(), partition, rd_kafka_last_error());
			}
			consumer.m_pKafkaTopic->updatePartitionStatusMap(partition, START_OK);	
			
		}

		int num = 0;
		do {
			num++;
			int ret = rd_kafka_consume_stop(consumer.m_pKafkaTopic->GetHandle(), partition);
			if (ret == 0) {
				INFO("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] stop success",
					consumer.GetAppKey().c_str(), consumer.GetTopic().c_str(), consumer.GetConsumerGroup().c_str(), partition);
				break;
			} else {

				ERROR("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] stop failed, errorcode:[%d]",
					consumer.GetAppKey().c_str(), consumer.GetTopic().c_str(), consumer.GetConsumerGroup().c_str(), partition, rd_kafka_last_error());
				//break;
				sleep(1);
			}
		}while(num < 4);
	}
	return true;
}


}

