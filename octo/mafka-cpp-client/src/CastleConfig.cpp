#include "CastleConfig.h"

#include "CommandExecutor.h"
#include "ProducerImpl.h"
#include "ClusterConsumerImpl.h"

#include "CastleClient.h"

#include "CommandHandler.h"

#include "Log.h"


namespace mafka
{


class FetchConfig : public CommandHandler
{
public:
	FetchConfig(ConfigChangedObserver& observer, int init_version);

public:
	virtual void HandleCommand();
	int GetVersion() const;

private:
	virtual int DoFetchConfig(Properties& properties, Clusters& clusters) = 0;

private:
	ConfigChangedObserver& m_observer;
	int m_iConfigVersion;
};

class FetchProducerConfig : public FetchConfig
{
public:
	FetchProducerConfig(ConfigChangedObserver& observer, int init_version, CastleConfig& config);

private:
	virtual int DoFetchConfig(Properties& properties, Clusters& clusters);

private:
	CastleConfig& m_config;
};

class FetchConsumerConfig : public FetchConfig
{
public:
	FetchConsumerConfig(ConfigChangedObserver& observer, int init_version, CastleConfig& config);

private:
	virtual int DoFetchConfig(Properties& properties, Clusters& clusters);

private:
	CastleConfig& m_config;
};


FetchConfig::FetchConfig(ConfigChangedObserver& observer, int init_version)
:m_observer(observer), m_iConfigVersion(init_version)
{

}

void FetchConfig::HandleCommand()
{
	INFO("FetchConfig start");
	Properties properties;
	Clusters clusters;
	int version = DoFetchConfig(properties, clusters);
	if(version == m_iConfigVersion)
	{
		return;
	}

	if(properties.empty())
	{
		ERROR("BUG: empty properties with version=%d\n", m_iConfigVersion);
		return;
	}

	if(clusters.empty())
	{
		ERROR("BUG: empty cluster with version=%d\n", m_iConfigVersion);
		return;
	}

	INFO("configurationi changed, prev version=%d, cur version=%d", m_iConfigVersion, version);

	m_observer.OnConfigChanged(properties, clusters);

	m_iConfigVersion = version;
	INFO("FetchConfig end");
}

int FetchConfig::GetVersion() const
{
	return m_iConfigVersion;
}


FetchProducerConfig::FetchProducerConfig(ConfigChangedObserver& observer, int init_version, CastleConfig& config)
:FetchConfig(observer, init_version), m_config(config)
{

}

int FetchProducerConfig::DoFetchConfig(Properties& properties, Clusters& clusters)
{
	int version = m_config.GetProperties(GetVersion(), properties, clusters);
	return version;
}

FetchConsumerConfig::FetchConsumerConfig(ConfigChangedObserver& observer, int init_version, CastleConfig& config)
:FetchConfig(observer, init_version), m_config(config)
{

}

int FetchConsumerConfig::DoFetchConfig(Properties& properties, Clusters& clusters)
{
	int version = m_config.GetProperties(GetVersion(), properties, clusters);
	return version;
}

CastleConfig::CastleConfig(std::string const& name_space, std::string const& appKey)
:m_pExecutor(new CommandExecutor("castle_heartbeat")),m_pFetchProducerConfig(NULL), m_pFetchConsumerConfig(NULL)
,m_pCastleClient(new CastleClient(name_space, appKey)), m_pFetchConfig(NULL)
{
	m_Running = false;
}

CastleConfig::~CastleConfig()
{
	delete m_pFetchConfig;
	delete m_pExecutor;
	delete m_pCastleClient;
}

void CastleConfig::Startup()
{
	m_Running = true;
	m_pExecutor->Start();
}

void CastleConfig::Shutdown()
{
	INFO("castle heartbeat shutdown [%p]", this);
	if(m_Running == false) {
		return;
	}
	m_Running = false;
	if(m_pFetchConfig)
	{
		m_pFetchConfig->Cancel();
	}

	m_pExecutor->Stop();
	
	m_pExecutor->WaitDone();
	INFO("castle heartbeat shutdown [%p] ok", this);
}

CommandExecutor& CastleConfig::GetExecutor() const
{
	return *m_pExecutor;
}

CastleClient& CastleConfig::GetCastleClient() const
{
	return *m_pCastleClient;
}

void CastleConfig::RegisterConfigChangedObserver(ConfigChangedObserver& observer, int init_version)
{
	INFO("register ConfigChangedObserver version=%d", init_version);
	if(m_pFetchConfig)
	{
		return;
	}


	//m_pFetchConfig = new FetchProducerConfig(observer, init_version, *this);
	m_pFetchConfig = NewFetchCommand(observer, init_version);
	GetExecutor().ExecuteEvery(*m_pFetchConfig, 1000*CastleConfig::HEART_BEAT_PERIOD);
	INFO("register ConfigChangedObserver over version=%d", init_version);
}

ProducerConfig::ProducerConfig(std::string const& name_space, ProducerImpl& producer)
:CastleConfig(name_space, producer.GetAppKey()), m_producer(producer)
{

}

ProducerConfig::~ProducerConfig()
{

}

int ProducerConfig::GetProperties(int version, Properties& properties, Clusters& clusters)
{

	int new_version = GetCastleClient().GetProducerProperties(m_producer.GetID(), version, m_producer.GetAppKey(), m_producer.GetTopic(), properties, clusters);

	return new_version;
}

CommandHandler* ProducerConfig::NewFetchCommand(ConfigChangedObserver& observer, int init_version)
{
	return new FetchProducerConfig(observer, init_version, *this);
}

ConsumerConfig::ConsumerConfig(std::string const& name_space, ClusterConsumerImpl& consumer)
:CastleConfig(name_space, consumer.GetAppKey()), m_consumer(consumer)
{

}

ConsumerConfig::~ConsumerConfig()
{

}

int ConsumerConfig::GetProperties(int version, Properties& properties, Clusters& clusters)
{
	int new_version = GetCastleClient().GetConsumerProperties(m_consumer.GetID(), version, m_consumer.GetAppKey(), m_consumer.GetTopic(), m_consumer.GetConsumerGroup(), properties, clusters);

	return new_version;
}

CommandHandler* ConsumerConfig::NewFetchCommand(ConfigChangedObserver& observer, int init_version)
{
	return new FetchConsumerConfig(observer, init_version, *this);
}


}

