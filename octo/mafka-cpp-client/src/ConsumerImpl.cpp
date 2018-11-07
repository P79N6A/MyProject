#include "ConsumerImpl.h"
#include "Mutex.h"
#include "Condition.h"
#include "LockGuard.h"
#include "CastleClient.h"

#include "CommandExecutor.h"
#include "CommandHandler.h"
#include "MessageHandler.h"
#include "ConsumerExecutor.h"

#include "Log.h"

#include "StringUtil.h"
#include "StringConverter.h"
#include "TimeUtil.h"

#include "KafkaInstance.h"
#include "KafkaTopic.h"
#include "PollCommand.h"
#include "StringDecoder.h"

#include <stdlib.h>

#include <castle/gen-cpp/CastleService.h>

namespace mafka
{

ConsumerImpl::ConsumerImpl(std::string const& appkey, std::string const& topic, std::string const& consumer_group)
:EndPointImpl(appkey, topic)
,m_pKafkaInstance(new KafkaInstance()), m_pKafkaTopic(new KafkaTopic())
,m_pPollCommand(new PollCommand(*m_pKafkaInstance))
,m_consumer_group(consumer_group)
,m_consumer_executors(), m_consumer_threads(1), m_bIsCallbackMode(true)
,m_partitionIndex(0), m_curPartition(0), m_cur_message(NULL)
{

}

ConsumerImpl::~ConsumerImpl()
{
	delete m_pKafkaTopic;
	delete m_pKafkaInstance;
}

std::string const& ConsumerImpl::GetConsumerGroup() const
{
	return m_consumer_group;
}

void ConsumerImpl::ApplyMessageHandler(HandlerProto& proto)
{
	for(int i = 0; i < static_cast<int>(m_partitions.size()); ++i)
	{
		int partition = m_partitions[i];
		ConsumerExecutor* executor = m_consumer_executors[i%m_consumer_threads];
		executor->Consume(*this, proto, partition);
	}
}

void ConsumerImpl::SetGeneratioinID(int generation_id)
{
	if(!m_pKafkaInstance)
	{
		return;
	}
	m_pKafkaInstance->SetGenerationID(generation_id);
}

void ConsumerImpl::SetCallbackMode(bool mode)
{
	m_bIsCallbackMode = mode;
}

void ConsumerImpl::StartConsume()
{
	if(!m_bIsCallbackMode)
	{
		for(int i = 0; i < static_cast<int>(m_partitions.size()); ++i)
		{	
			int partition = m_partitions[i];
			if (rd_kafka_consume_start(m_pKafkaTopic->GetHandle(), partition, RD_KAFKA_OFFSET_STORED) == -1)
			{
				ERROR("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] startup failed, err=%d",
				    GetAppKey().c_str(), GetTopic().c_str(), GetConsumerGroup().c_str(), partition, rd_kafka_last_error());
			}
			m_pKafkaTopic->updatePartitionStatusMap(partition, START_OK);	
			INFO("update status ok handle:[%p], partition:[%d]\n",
				m_pKafkaTopic->GetHandle(), partition);	
		}
		return;
	}
	if(!m_consumer_executors.empty())
	{
		ERROR("BUG, threads already started=%d", m_consumer_executors.size());
	}

	for(int i = 0; i < m_consumer_threads; ++i)
	{
		ConsumerExecutor* executor = new ConsumerExecutor(m_consumer_group);
		executor->Startup();
		m_consumer_executors.push_back(executor);
	}
}
char* ConsumerImpl::GetMessage(int timeout, int& len)
{	
	//get net parition to consume
	int partition_size = m_partitions.size();
	if(partition_size <= 0){
		return NULL;
	}
	m_partitionIndex = (++m_partitionIndex)%partition_size;
	m_curPartition = m_partitions[m_partitionIndex];
	//get message
	INFO("message parition %d", m_curPartition);
	if(NULL == m_cur_message)
	{
		m_cur_message = rd_kafka_consume(m_pKafkaTopic->GetHandle(), m_curPartition, timeout);
		if(NULL == m_cur_message){//timeout
			return NULL;
		}
		if(m_cur_message->err)
		{
			ERROR("rd_kafka_consume message failed, partition=%d, errcode=%d, errorstr=%s, offset=%llu\n",
				m_cur_message->partition, m_cur_message->err, (char*)m_cur_message->payload, m_cur_message->offset);
			if(m_cur_message->err == -191)
			{
				rd_kafka_t* rd_kafka_t = m_pKafkaInstance->GetHandle();
				rd_kafka_commit_message(rd_kafka_t, m_cur_message, 1);
			}
			rd_kafka_message_destroy(m_cur_message);
			m_cur_message = NULL;
			return NULL;
		}
	}
	else
	{
		WARN("message not commit, return current message");	
	}
	char* dstContent = NULL;
	int msgLen = 0;
	StringDecoder::stringDecode(static_cast<const char*>(m_cur_message->payload), dstContent, msgLen);
	INFO("message partition:[%d] messageContent:[%s] len:[%d]", m_curPartition, dstContent, msgLen);
	len = msgLen;
	
	return dstContent;
}

bool ConsumerImpl::Commit()
{
	if(NULL == m_cur_message)
	{	
		return true;
	}
	rd_kafka_t* rd_kafka_t = m_pKafkaInstance->GetHandle();
	INFO("commit message [%p], parition=%d, offset=%llu\n", rd_kafka_t, m_cur_message->partition, m_cur_message->offset);
	if(!m_cur_message->err)
	{
		rd_kafka_offset_store(m_cur_message->rkt, m_cur_message->partition, m_cur_message->offset);
	}
	rd_kafka_resp_err_t ret = rd_kafka_commit_message(rd_kafka_t, m_cur_message, 1);
	if(RD_KAFKA_RESP_ERR_NO_ERROR != ret)
	{
		ERROR("commit offset failed, parition=%d, offset=%llu, error_code=%d\n", m_cur_message->partition, m_cur_message->offset, ret);
		return false;
	}
	rd_kafka_message_destroy(m_cur_message);
	m_cur_message = NULL;
	return true;
}
void ConsumerImpl::StopConsume()
{
	if(!m_bIsCallbackMode)
	{
		
		for(int i = 0; i < static_cast<int>(m_partitions.size()); ++i)
		{	
			int partition = m_partitions[i];
			int retry_count = 0;
			do
			{
				if (rd_kafka_consume_stop(m_pKafkaTopic->GetHandle(), partition) != 0)
				{
					ERROR("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] stop failed, errorcode:[%d]",
						GetAppKey().c_str(), GetTopic().c_str(), GetConsumerGroup().c_str(), partition, rd_kafka_last_error());
					sleep(1);
				}
				else
				{
					INFO("consumer [appkey=%s, topic=%s, cg=%s, partition=%d] stop",
						GetAppKey().c_str(), GetTopic().c_str(), GetConsumerGroup().c_str(), partition);
					break;
				}
			}while(retry_count++ < 4);
		}
	}
	for(ConsumerExecutors::iterator i = m_consumer_executors.begin(); i != m_consumer_executors.end(); ++i)
	{
		ConsumerExecutor* executor = *i;
		executor->CancelConsume();
		executor->StopExecutor();
	}
	
	for(ConsumerExecutors::iterator i = m_consumer_executors.begin(); i != m_consumer_executors.end(); ++i)
	{
		ConsumerExecutor* executor = *i;
		executor->Reset(*this);
		executor->Shutdown();
		delete executor;
	}

	m_consumer_executors.clear();
}

ErrorCode ConsumerImpl::OnOPen(Properties const& global_properties, Properties const& topic_properties)
{
	bool ret = m_pKafkaInstance->Create(false, global_properties);
	if(!ret)
	{
		INFO("consumer [appkey=%s, topic=%s, cg=%s] create instance failed!!!", GetAppKey().c_str(), GetTopic().c_str(), m_consumer_group.c_str());
		return RD_KAFKA_RESP_ERR__FAIL;
	}
	ret = m_pKafkaTopic->Create(*m_pKafkaInstance, GetTopic(), topic_properties);
	if(!ret)
	{
		INFO("consumer [appkey=%s, topic=%s, cg=%s] create topic failed!!!", GetAppKey().c_str(), GetTopic().c_str(), m_consumer_group.c_str());
		return RD_KAFKA_RESP_ERR__FAIL;
	}

	GetExecutor().Execute(*m_pPollCommand);

	StartConsume();

	INFO("consumer [appkey=%s, topic=%s, cg=%s] initialize success!!!", GetAppKey().c_str(), GetTopic().c_str(), m_consumer_group.c_str());

	return RD_KAFKA_RESP_ERR_NO_ERROR;
}

void ConsumerImpl::OnClose()
{	
	
	INFO("poll cancel command start");	
	m_pPollCommand->Cancel();
	
	if(m_pPollCommand->WaitRunning() == false) {
		WARN("poll command not stop");
	}	

	INFO("poll cancel command end");	
	
	StopConsume();
        INFO("consume stoped");
		
	m_pKafkaInstance->WaitOutQEmpty();
	m_pKafkaTopic->Destroy();
	m_pKafkaInstance->Destroy();
	
	
	m_pKafkaInstance = NULL;
}

void ConsumerImpl::OnFixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions)
{
	properties.insert(Properties::value_type("group.id", m_consumer_group));

	//process threads changes
	{
		Properties::const_iterator i = properties.find("consumer.thread.num");
		if(i != properties.end())
		{
			int cur_threads = StringConverter::ParseInt(i->second);
			INFO("consumer prev thread=%d, cur thread=%d", m_consumer_threads, cur_threads);
			m_consumer_threads = cur_threads;
		}
	}

	if(partitions.empty())
	{
		INFO("consumer partition empty");
	}

	// process partition changes
	m_partitions = partitions;
}

void ConsumerImpl::OnFilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties)
{
	//global configurations
	std::vector<std::string> globals;
	globals.push_back("group.id");
	globals.push_back("client.id");
	globals.push_back("fetch.min.bytes");
	globals.push_back("fetch.wait.max.ms");
	globals.push_back("socket.receive.buffer.bytes");
	globals.push_back("socket.send.buffer.bytes");
	globals.push_back("topic.metadata.refresh.interval.ms");
	globals.push_back("enable.auto.offset.store");
	for(std::vector<std::string>::iterator i = globals.begin(); i != globals.end(); ++i)
	{
		std::string const& key = *i;
		Properties::const_iterator j = properties.find(key);
		if(j != properties.end())
		{
			global_properties.insert(Properties::value_type(j->first, j->second));
		}
	}
	global_properties["enable.auto.offset.store"] = "false";

	//topic configurations
	std::vector<std::string> topics;
	topics.push_back("auto.commit.enable");
	topics.push_back("auto.commit.interval.ms");
	topics.push_back("auto.offset.reset");
	for(std::vector<std::string>::iterator i = topics.begin(); i != topics.end(); ++i)
	{
		std::string const& key = *i;
		Properties::const_iterator j = properties.find(key);
		if(j != properties.end())
		{
			topic_properties.insert(Properties::value_type(j->first, j->second));
		}
	}

	topic_properties["auto.commit.enable"] = "false";
}

bool ConsumerImpl::CheckAllMessageDone() const
{
	return true;
}

}

