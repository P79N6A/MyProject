#ifndef __MAFKA_CONSUMER_IMPL_H__
#define __MAFKA_CONSUMER_IMPL_H__

#include "EndPointImpl.h"
#include <vector>

namespace mafka
{

class MessageHandler;
class CommandHandler;
class ConsumerExecutor;
class HandlerProto;

class KafkaInstance;
class KafkaTopic;

class ConsumerImpl : public EndPointImpl
{
public:
	typedef std::vector<int> Partitions;

public:
	ConsumerImpl(std::string const& appkey, std::string const& topic, std::string const& consumer_group);
	virtual ~ConsumerImpl();

public:
	std::string const& GetConsumerGroup() const;

public:
	void ApplyMessageHandler(HandlerProto& proto);
	char* GetMessage(int timeout, int& len);
	bool Commit();
public:
	void SetGeneratioinID(int generation_id);
	void SetCallbackMode(bool mode);
private:
	void StartConsume();
	void StopConsume();

private:
	virtual ErrorCode OnOPen(Properties const& global_properties, Properties const& topic_properties);
	virtual void OnClose();

	virtual void OnFixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions);
	virtual void OnFilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties);

	virtual bool CheckAllMessageDone() const;

private:
	friend class ConsumerInstance;
	static void msg_complete(rd_kafka_t *rk, const rd_kafka_message_t *rkmessage, void *opaque);

private:
	KafkaInstance* m_pKafkaInstance;
	KafkaTopic* m_pKafkaTopic;
	CommandHandler* m_pPollCommand;

	std::string m_consumer_group;

	typedef std::vector<ConsumerExecutor*> ConsumerExecutors;
	ConsumerExecutors m_consumer_executors;
	int m_consumer_threads;
	friend class ConsumeCommand;
	friend class ConsumerExecutor;

	Partitions m_partitions;
	bool m_bIsCallbackMode;
	int m_partitionIndex;
	int m_curPartition;
	rd_kafka_message_t* m_cur_message;
};



}



#endif //__MAFKA_CONSUMER_IMPL_H__
