#include "KafkaTopic.h"
#include "KafkaInstance.h"
#include "LockGuard.h"

#include "Log.h"

namespace mafka
{

KafkaTopic::KafkaTopic()
:m_kafka_topic(NULL)
{
	m_partitionIdStatusMap.clear();
	m_pmutex = new Mutex();
}

KafkaTopic::~KafkaTopic()
{
	Destroy();
	delete m_pmutex;
}

bool KafkaTopic::existPartitionId(int partitionId) {
	LockGuard guard(*m_pmutex);
	std::map<int, PartitionStatus>::const_iterator it;
	it = m_partitionIdStatusMap.find(partitionId);
	if ( it == m_partitionIdStatusMap.end() ) {
		return false;
	}
	return true;
}

void KafkaTopic::insertPartitionStatusMap(int partitionId, PartitionStatus partitionStatus) {
	LockGuard guard(*m_pmutex);
	m_partitionIdStatusMap.insert(std::pair<int, PartitionStatus>(partitionId, partitionStatus));
}

void KafkaTopic::erasePartitionMap(int partitionId) {
	LockGuard guard(*m_pmutex);
	m_partitionIdStatusMap.erase(partitionId);
}

PartitionStatus KafkaTopic::getPartitionStatus(int partitionId) {
	std::map<int, PartitionStatus>::const_iterator it;
	it = m_partitionIdStatusMap.find(partitionId);
	return it->second;
}

void KafkaTopic::updatePartitionStatusMap(int partitionId, PartitionStatus partitionStatus) {
	bool bExist = existPartitionId(partitionId);
	if(bExist) {
		erasePartitionMap(partitionId);	
	}
	
	insertPartitionStatusMap(partitionId, partitionStatus);
}	


bool KafkaTopic::Create(KafkaInstance const& instance, std::string const& topic, Properties const& topic_properties)
{
	if(m_kafka_topic)
	{
		return true;
	}

	//create topic
	rd_kafka_topic_conf_t* topic_conf = rd_kafka_topic_conf_new();
	if(!topic_conf)
	{
		ERROR("rd_kafka_topic_conf_new failed=%d\n", RD_KAFKA_RESP_ERR__FAIL);
		return false;
	}
	SetConfigurations(topic_conf, topic_properties);

	m_kafka_topic = rd_kafka_topic_new(instance.GetHandle(), topic.c_str(), topic_conf);
	if(!m_kafka_topic)
	{
		ERROR("rd_kafka_topic_new failed=%d\n", rd_kafka_last_error());
		return false;
	}

	INFO("kafka_topic tar:[%p] ,src:[%p]", m_kafka_topic, instance.GetHandle());
	return true;

}

void KafkaTopic::Destroy()
{
	if(!m_kafka_topic)
	{
		return;
	}

	rd_kafka_topic_destroy(m_kafka_topic);
	m_kafka_topic = NULL;
}

rd_kafka_topic_t* KafkaTopic::GetHandle() const
{
	return m_kafka_topic;
}

bool KafkaTopic::SetConfigurations(rd_kafka_topic_conf_t* conf, Properties const& properties)
{
	char errstr[512];
	for(Properties::const_iterator i = properties.begin(); i != properties.end(); ++i)
	{
		std::string const& key = i->first;
		std::string const& value = i->second;
		rd_kafka_conf_res_t ret = rd_kafka_topic_conf_set(conf, key.c_str(), value.c_str(), errstr, sizeof(errstr));
		if(ret != RD_KAFKA_CONF_OK)
		{
			ERROR("failed to set %s:%s, error=%s\n", key.c_str(), value.c_str(), errstr);
			return false;
		}
	}

	return true;
}



}

