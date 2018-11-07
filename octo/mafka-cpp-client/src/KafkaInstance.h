#ifndef __MAFKA_KAFKA_INSTANCE_H__
#define __MAFKA_KAFKA_INSTANCE_H__

#include <rdkafka.h>

#include "CastleConfig.h"

namespace mafka
{

class KafkaInstance
{

public:
	typedef void (*MessageCallback)(rd_kafka_t *rk, const rd_kafka_message_t *rkmessage, void *opaque);

public:
	KafkaInstance();
	~KafkaInstance();

public:
	bool Create(bool is_producer, Properties const& global_properties, MessageCallback callback = NULL);
	void Destroy();

	void SetGenerationID(int generation_id);

	rd_kafka_t* GetHandle() const;

	void PollEvent(int timeout);
	
	void WaitOutQEmpty();

private:
	bool SetConfigurations(rd_kafka_conf_t* conf, Properties const& properties);

private:
	rd_kafka_t* m_kafka_instance;
};



}



#endif //__MAFKA_KAFKA_INSTANCE_H__
