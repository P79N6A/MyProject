#include "KafkaInstance.h"
#include "TimeUtil.h"

#include "Log.h"

namespace mafka
{

KafkaInstance::KafkaInstance()
:m_kafka_instance(NULL)
{

}

KafkaInstance::~KafkaInstance()
{
	Destroy();
}

bool KafkaInstance::Create(bool is_producer, Properties const& global_properties, MessageCallback callback/* = NULL*/)
{
	if(m_kafka_instance != NULL)
	{
		return true;
	}

	rd_kafka_conf_t* conf = rd_kafka_conf_new();
	if(!conf)
	{
		ERROR("rd_kafka_conf_new failed\n");
		return false;
	}

	bool ret = SetConfigurations(conf, global_properties);
	if(!ret)
	{
		ERROR("set configuratioins failed=%d, some configuration may not take effect\n", rd_kafka_last_error());
	}

	if(callback)
	{
		rd_kafka_conf_set_dr_msg_cb(conf, callback);
	}

	char errstr[512];
	m_kafka_instance = rd_kafka_new(is_producer?RD_KAFKA_PRODUCER:RD_KAFKA_CONSUMER, conf, errstr, sizeof(errstr));
	if(!m_kafka_instance)
	{
		ERROR("rd_kafka_new failed=%d\n", rd_kafka_last_error());
		return false;
	}

	INFO("new kafka_instance [%p]", m_kafka_instance);
	return true;
}

void KafkaInstance::WaitOutQEmpty() {

	//wait max 10 seconds time
	const int MAX_WAIT_TIME = 10*1000;
	timestamp_t start = TimeUtil::TimeNow();
	while (rd_kafka_outq_len(m_kafka_instance) > 0)
	{
		PollEvent(10);
		timestamp_t now = TimeUtil::TimeNow();
		if( (now-start) > MAX_WAIT_TIME )
		{
			ERROR("instance waiting out_queue timeout=%d len=%d\n", MAX_WAIT_TIME, rd_kafka_outq_len(m_kafka_instance));
			break;
		}
	}
	INFO("[%p] queue len : [%d]", this, rd_kafka_outq_len(m_kafka_instance));
}

void KafkaInstance::Destroy()
{
	if(!m_kafka_instance)
	{
		return;
	}
	//WaitOutQEmpty();
	rd_kafka_destroy(m_kafka_instance);
	m_kafka_instance = NULL;

	int ret = rd_kafka_wait_destroyed(1000);
	INFO("destory return :[%d]",ret);
}

void KafkaInstance::SetGenerationID(int generation_id)
{
	if(!m_kafka_instance)
	{
		return;
	}

	rd_kafka_set_generationid(m_kafka_instance, generation_id);
}

rd_kafka_t* KafkaInstance::GetHandle() const
{
	return m_kafka_instance;
}

void KafkaInstance::PollEvent(int timeout)
{
	if(!m_kafka_instance)
	{
		return;
	}

	rd_kafka_poll(m_kafka_instance, timeout);
}

bool KafkaInstance::SetConfigurations(rd_kafka_conf_t* conf, Properties const& properties)
{
	char errstr[512];
	for(Properties::const_iterator i = properties.begin(); i != properties.end(); ++i)
	{
		std::string const& key = i->first;
		std::string const& value = i->second;
		rd_kafka_conf_res_t ret = rd_kafka_conf_set(conf, key.c_str(), value.c_str(), errstr, sizeof(errstr));
		if(ret != RD_KAFKA_CONF_OK)
		{
			ERROR("failed to set %s:%s, error=%s\n", key.c_str(), value.c_str(), errstr);
			return false;
		}
	}

	return true;
}



}

