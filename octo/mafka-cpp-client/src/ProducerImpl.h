#ifndef __MAFKA_PRODUCER_IMPL_H__
#define __MAFKA_PRODUCER_IMPL_H__

#include "EndPointImpl.h"
#include "CompleteHandler.h"
#include "CastleConfig.h"
#include "IDGenerator.h"
#include <vector>

namespace mafka
{

class Mutex;
class Condition;
class KafkaTopic;
class ProducerImpl : public EndPointImpl, public IDGenerator, private ConfigChangedObserver
{
public:
	ProducerImpl(std::string const& appkey, std::string const& topic, std::string const& name_space);
	virtual ~ProducerImpl();

public:
	ErrorCode Open();
	void Close();
	void Cancel();
	void ForbidSerializable();
public:
	ErrorCode Send(const char* buf, int len, int timeout);
	ErrorCode SendMessage(const char* buf, int len, int timeout);
	ErrorCode SendAsync(const char* buf, int len, CompletionHandler& handler);
	ErrorCode SendAsyncMessage(const char* buf, int len, CompletionHandler& handler);

	ErrorCode Send(int partition, const char* buf, int len, int timeout);
	ErrorCode SendMessage(int partition, const char* buf, int len, int timeout);
	ErrorCode SendAsync(int partition, const char* buf, int len, CompletionHandler& handler);
	ErrorCode SendAsyncMessage(int partition, const char* buf, int len, CompletionHandler& handler);

private:
	ErrorCode SafeProduce(int partition, const char* buf, int len, void* msg_opaque);
	int GetNextPartition();
	int CheckPartition(int partition);
private:
	virtual ErrorCode OnOPen(Properties const& global_properties, Properties const& topic_properties);
	virtual void OnClose();

	virtual void OnFixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions);
	virtual void OnFilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties);

	virtual bool CheckAllMessageDone() const;

private:
	virtual void OnConfigChanged(Properties const& properties, Clusters const& clusters);

private:
	static void msg_complete(rd_kafka_t *rk, const rd_kafka_message_t *rkmessage, void *opaque);

private:
	CastleConfig* m_pCastleConfig;
	
	KafkaTopic* m_pKafkaTopic;
	CommandHandler* m_pPollCommand;

	//for sync sending message
	Mutex* m_pSyncMutex;
	Condition* m_pSyncCondition;
	ErrorCode m_sync_error_code;

	//for gracefull shutdown
	Mutex* m_pMessageDoneMutex;
	Condition* m_pMessageDoneCondition;
	int m_ongoing_messages;
	int m_done_messages;

	int m_default_partition;
	int m_partition_count;
	int m_partition_sending;
	bool m_startup_broker;

	//for serialization
	bool m_serializable;
};



}



#endif //__MAFKA_PRODUCER_IMPL_H__
