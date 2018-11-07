#include "ProducerImpl.h"
#include "Mutex.h"
#include "Condition.h"
#include "LockGuard.h"
#include "CastleClient.h"

#include "PollCommand.h"
#include "CommandExecutor.h"
#include "KafkaInstance.h"
#include "KafkaTopic.h"


#include "Log.h"

#include "StringUtil.h"
#include "StringConverter.h"
#include "StringEncoder.h"
#include "StringDecoder.h"
#include "TimeUtil.h"

#include <stdlib.h>

#include <castle/gen-cpp/CastleService.h>

namespace mafka
{

static Mutex ProducerInstanceMutex;
static KafkaInstance g_ProducerInstance;

ProducerImpl::ProducerImpl(std::string const& appkey, std::string const& topic, std::string const& name_space)
:EndPointImpl(appkey, topic)
,m_pCastleConfig(new ProducerConfig(name_space, *this))
,m_pKafkaTopic(new KafkaTopic())
,m_pPollCommand(new PollCommand(g_ProducerInstance))
,m_pSyncMutex(new Mutex()), m_pSyncCondition(new Condition()), m_sync_error_code(RD_KAFKA_RESP_ERR_NO_ERROR)
,m_pMessageDoneMutex(new Mutex), m_pMessageDoneCondition(new Condition), m_ongoing_messages(0), m_done_messages(0)
,m_default_partition(RD_KAFKA_PARTITION_UA), m_partition_count(0), m_partition_sending(0),m_startup_broker(false),m_serializable(true)
{
}

ProducerImpl::~ProducerImpl()
{
	delete m_pCastleConfig;

	delete m_pKafkaTopic;

	delete m_pSyncMutex;
	delete m_pSyncCondition;

	delete m_pMessageDoneMutex;
	delete m_pMessageDoneCondition;

}

ErrorCode ProducerImpl::Open()
{
	Properties properties;
	Clusters clusters;
	int retry_count = 0;
	int MAX_RETRIES = 3;
	int version = -1;
	while(retry_count < MAX_RETRIES)
	{
		version = m_pCastleConfig->GetProperties(-1, properties, clusters);
		if(version != -1)
		{
			break;
		}
		++retry_count;
		sleep(1);
	}
	if(version == -1)
	{
		ERROR("castle fetch configuration faied, can not startup\n");
		return RD_KAFKA_RESP_ERR__FAIL;
	}
	
	Brokers const& brokers = clusters[0].brokers;
	Partitions const& partitions = clusters[0].partitions;
	ErrorCode ret = DoOpen(properties, brokers, partitions);
	m_startup_broker = true;
	if(ret != RD_KAFKA_RESP_ERR_NO_ERROR)
	{
		ERROR("endpoint startup faied\n");
		return ret;
	}

	m_pCastleConfig->RegisterConfigChangedObserver(*this, version);
	m_pCastleConfig->Startup();
	INFO("endpoint [appkey=%s, topic=%s] startup success\n", GetAppKey().c_str(), GetTopic().c_str());

	return ret;

}

void ProducerImpl::Close()
{

	DoClose(false);
}

struct Message
{
	Message()
	:wakeup_cond(NULL), impl(NULL), completion_handler(NULL)
	{

	}

	Condition* wakeup_cond;
	ProducerImpl* impl;

	CompletionHandler* completion_handler;
};
int ProducerImpl::GetNextPartition(){
	int current = m_partition_sending;//copy value to local, avoid lock
	int partition_count = m_partition_count;
	int next_partition = m_default_partition;
	if(partition_count > 0)
	{
		next_partition = abs(current + 1) % partition_count;
		m_partition_sending = next_partition;
	}
	return next_partition;
}

ErrorCode ProducerImpl::Send(const char* buf, int len, int timeout)
{
	return Send(m_default_partition, buf, len, timeout);
}

ErrorCode ProducerImpl::SendMessage(const char* buf, int len, int timeout)
{
        return Send(GetNextPartition(), buf, len, timeout);
}

ErrorCode ProducerImpl::SendAsync(const char* buf, int len, CompletionHandler& handler)
{
	return SendAsync(m_default_partition, buf, len, handler);
}

ErrorCode ProducerImpl::SendAsyncMessage(const char* buf, int len, CompletionHandler& handler)
{
        return SendAsync(GetNextPartition(), buf, len, handler);
}

int ProducerImpl::CheckPartition(int partition)
{
	int partition_count = m_partition_count;
	int partition_checked = m_default_partition;
	if(partition_count > 0)
	{
		partition_checked = (abs(partition)) % partition_count;
	}
	return partition_checked;
}

ErrorCode ProducerImpl::Send(int partition, const char* buf, int len, int timeout)
{
	Message* msg = new Message();
	msg->wakeup_cond = m_pSyncCondition;
	msg->impl = this;
	int partition_checked = CheckPartition(partition);
	
	ErrorCode err = SafeProduce(partition_checked, buf, len, msg);
	if(err)
	{
		ERROR("ProducerImpl::Send failed, err=%d\n", err);
		delete msg;
		return err;
	}

	LockGuard guard(*m_pSyncMutex);
	bool timeout_happen = m_pSyncCondition->TimedWait(*m_pSyncMutex, timeout);
	if(timeout_happen)
	{
		ERROR("ProducerImpl::Send timeout happen, timeout=%d\n", timeout);
		return RD_KAFKA_RESP_ERR__MSG_TIMED_OUT;
	}

	return m_sync_error_code;
}

ErrorCode ProducerImpl::SendMessage(int partition, const char* buf, int len, int timeout)
{
	return Send(partition, buf, len, timeout);
}

ErrorCode ProducerImpl::SendAsync(int partition, const char* buf, int len, CompletionHandler& handler)
{
	Message* msg = new Message();
	msg->completion_handler = &handler;
	msg->impl = this;
	int partition_checked = CheckPartition(partition);
	ErrorCode err = SafeProduce(partition_checked, buf, len, msg);
	if(err)
	{
		ERROR("ProducerImpl::SendAsync failed, err=%d\n", err);
		delete msg;
		return err;
	}

	return RD_KAFKA_RESP_ERR_NO_ERROR;
}

ErrorCode ProducerImpl::SendAsyncMessage(int partition, const char* buf, int len, CompletionHandler& handler)
{
	return SendAsync(partition, buf, len, handler);	
}

ErrorCode ProducerImpl::SafeProduce(int partition, const char* buf, int len, void* msg_opaque)
{
	if(CheckRestarting())
	{
		ERROR("ProducerImpl::SafeProduce dropped because of restarting\n");
		return RD_KAFKA_RESP_ERR__FAIL;
	}

	LockGuard guard(GetRestartMutex());
	if(CheckRestarting())
	{
		ERROR("ProducerImpl::SafeProduce dropped because of restarting\n");
		return RD_KAFKA_RESP_ERR__FAIL;
	}


	if(m_serializable){	
		char* dst = NULL;
		int dst_len = 0;
		StringEncoder::stringEncode(const_cast<const char*>(buf), len, dst, dst_len);
		int ret = rd_kafka_produce(m_pKafkaTopic->GetHandle(), partition/*RD_KAFKA_PARTITION_UA*/, RD_KAFKA_MSG_F_COPY, dst, dst_len, NULL, 0, msg_opaque);
		if (ret == -1)
		{
			ERROR("rd_kafka_produce err=%d\n", ret);
			free(dst);
			return rd_kafka_last_error();
		}
		free(dst);
	}else{
		char* dst = const_cast<char*>(buf);
		int ret = rd_kafka_produce(m_pKafkaTopic->GetHandle(), partition/*RD_KAFKA_PARTITION_UA*/, RD_KAFKA_MSG_F_COPY, dst, len, NULL, 0, msg_opaque);
		if (ret == -1)
		{
			ERROR("rd_kafka_produce err=%d\n", ret);
			return rd_kafka_last_error();
		}
	}

	++m_ongoing_messages;
	return RD_KAFKA_RESP_ERR_NO_ERROR;
}

ErrorCode ProducerImpl::OnOPen(Properties const& global_properties, Properties const& topic_properties)
{
	{
		LockGuard guard(ProducerInstanceMutex);
		if(!g_ProducerInstance.Create(true, global_properties, ProducerImpl::msg_complete))
		{
			ERROR("Producer Instance create failed=%d\n", RD_KAFKA_RESP_ERR__FAIL);
			return RD_KAFKA_RESP_ERR__FAIL;
		}
	}

	//create topic
	bool ret = m_pKafkaTopic->Create(g_ProducerInstance, GetTopic(), topic_properties);
	if(!ret)
	{
		INFO("producer [appkey=%s, topic=%s] create topic failed!!!", GetAppKey().c_str(), GetTopic().c_str());
		return RD_KAFKA_RESP_ERR__FAIL;
	}
	INFO("producer [appkey=%s, topic=%s] initialize success!!!", GetAppKey().c_str(), GetTopic().c_str());
	GetExecutor().Execute(*m_pPollCommand);

	return RD_KAFKA_RESP_ERR_NO_ERROR;
}

void ProducerImpl::Cancel() 
{
	m_pCastleConfig->Shutdown();

	m_pPollCommand->Cancel();
}


void ProducerImpl::OnClose()
{
	if(!m_startup_broker){
		INFO("broker no start up, no need to close");
		return;
	}

	INFO("poll cancel command start");	
	m_pPollCommand->Cancel();
	
	if(m_pPollCommand->WaitRunning() == false) {
		WARN("poll command not stop");
	}	

	INFO("poll cancel command end");	


	//waiting message process done, 
	{
		LockGuard guard(*m_pMessageDoneMutex);
		if(m_done_messages < m_ongoing_messages)
		{
			const int MAX_WAIT_TIME = 10*1000;
			m_pMessageDoneCondition->TimedWait(*m_pMessageDoneMutex, MAX_WAIT_TIME);
		}
	}

        g_ProducerInstance.WaitOutQEmpty();	
	m_pKafkaTopic->Destroy();
	INFO("producer [appkey=%s, topic=%s] closed with error=%d. m_ongoing_message=%d, m_done_messages=%d\n",
			GetAppKey().c_str(), GetTopic().c_str(), 
			rd_kafka_last_error(), m_ongoing_messages, m_done_messages);

	m_ongoing_messages = 0;
	m_done_messages = 0;

}

void ProducerImpl::OnConfigChanged(Properties const& properties, Clusters const& clusters)
{
	Brokers const& brokers = clusters[0].brokers;
	Partitions const& partitions = clusters[0].partitions;
	DoRestart(properties, brokers, partitions);
}

void ProducerImpl::OnFixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions)
{
	if(!partitions.empty())
	{
		m_default_partition = partitions[TimeUtil::TimeNow()%partitions.size()];
		m_partition_count = partitions.size();
	}
	else
	{
		m_default_partition = RD_KAFKA_PARTITION_UA;
	}

	INFO("[appkey=%s, topic=%s] new default_partition is %d, size=%d\n",
		GetAppKey().c_str(), GetTopic().c_str(), m_default_partition, partitions.size());
}

void ProducerImpl::OnFilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties)
{
	//global configurations
	std::vector<std::string> globals;
	globals.push_back("queue.buffering.max.messages");
	globals.push_back("queue.buffering.max.ms");
	globals.push_back("compression.codec");
	globals.push_back("compressed.topics");
	globals.push_back("message.send.max.retries");
	globals.push_back("retry.backoff.ms");
	globals.push_back("topic.metadata.refresh.interval.ms");

	for(std::vector<std::string>::iterator i = globals.begin(); i != globals.end(); ++i)
	{
		std::string const& key = *i;
		Properties::const_iterator j = properties.find(key);
		if(j != properties.end())
		{
			global_properties.insert(Properties::value_type(j->first, j->second));
		}
	}
	//config name different, convert
	Properties::const_iterator j = properties.find("buffer.memory");
	if(j != properties.end())
	{
		char str[10];
		sprintf(str,"%d",atoi(j->second.c_str())/1024);
		global_properties.insert(Properties::value_type("queue.buffering.max.kbytes", str));//bytes to kbytes
	}
	//topic configurations
	std::vector<std::string> topics;
	topics.push_back("request.required.acks");
	for(std::vector<std::string>::iterator i = topics.begin(); i != topics.end(); ++i)
	{
		std::string const& key = *i;
		Properties::const_iterator j = properties.find(key);
		if(j != properties.end())
		{
			topic_properties.insert(Properties::value_type(j->first, j->second));
		}
	}
}

bool ProducerImpl::CheckAllMessageDone() const
{
	return m_done_messages >= m_ongoing_messages;
}

void ProducerImpl::msg_complete(rd_kafka_t *rk,
                            const rd_kafka_message_t *rkmessage, void *opaque)
{
	Message* msg = static_cast<Message*>(rkmessage->_private);

	//for sync send, the msg is is freed before send return
	if(msg->wakeup_cond)
	{
		msg->impl->m_sync_error_code = rkmessage->err;
		msg->wakeup_cond->Signal();
	}

	//for async send, the msg is useless after callback, so we delete it
	if(msg->completion_handler)
	{
		//char* dstContent = (char*)calloc(sizeof(char), rkmessage->len);
		char* dstContent = NULL;
		int len = 0;
	    StringDecoder::stringDecode(static_cast<const char*>(rkmessage->payload), dstContent, len);	
		msg->completion_handler->OnComplete(rkmessage->err, rkmessage->partition, static_cast<const char*>(dstContent), len);
	}

	++msg->impl->m_done_messages;
	if(msg->impl->m_done_messages >= msg->impl->m_ongoing_messages)
	{
		msg->impl->m_pMessageDoneCondition->Signal();
	}

	delete msg;

}

void ProducerImpl::ForbidSerializable()
{
	m_serializable = false;
}

}

