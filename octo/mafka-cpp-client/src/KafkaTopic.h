#ifndef __MAFKA_KAFKA_TOPIC_H__
#define __MAFKA_KAFKA_TOPIC_H__

#include <rdkafka.h>
#include <string>
#include <map>
#include "Mutex.h"

#include "CastleConfig.h"


namespace mafka
{

enum PartitionStatus
{
	START_OK = 1,
	STOPPING = 2,
	STOP_OK = 3
};

class KafkaInstance;
class KafkaTopic
{
public:
	typedef std::map<int, PartitionStatus> PartitionIdStatusMap;		

public:
	KafkaTopic();
	~KafkaTopic();

public:
	bool Create(KafkaInstance const& instance, std::string const& topic, Properties const& topic_properties);
	void Destroy();

	rd_kafka_topic_t* GetHandle() const;
	
	bool existPartitionId(int partitionId);
	void insertPartitionStatusMap(int partitionId, PartitionStatus partitionStatus);
	PartitionStatus getPartitionStatus(int partitionId);

	void updatePartitionStatusMap(int partitionId, PartitionStatus partitionStatus);
	void erasePartitionMap(int partitionId);

private:
	bool SetConfigurations(rd_kafka_topic_conf_t* conf, Properties const& properties);

private:
	rd_kafka_topic_t* m_kafka_topic;
	PartitionIdStatusMap m_partitionIdStatusMap;
	Mutex* m_pmutex;
};



}



#endif //__MAFKA_KAFKA_TOPIC_H__
