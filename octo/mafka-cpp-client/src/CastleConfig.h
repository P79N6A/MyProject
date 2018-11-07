#ifndef __MAFKA_CASTLE_CONFIG_H__
#define __MAFKA_CASTLE_CONFIG_H__

#include <string>
#include <vector>
#include <map>

namespace mafka
{


struct BrokerConfig
{
	std::string host;
	int port;
};

typedef std::map<std::string, std::string> Properties;
typedef std::vector<BrokerConfig> Brokers;
typedef std::vector<int> Partitions;

struct ClusterConfig
{
	std::string name;
	Brokers brokers;
	int generation_id;
	Partitions partitions;
};
typedef std::vector<ClusterConfig> Clusters;

class ConfigChangedObserver
{
public:
	virtual void OnConfigChanged(Properties const& properties, Clusters const& clusters) = 0;
};

class CommandExecutor;
class CommandHandler;
class CastleClient;
class CastleConfig
{
public:
CastleConfig(std::string const& name_space, std::string const& appKey);
	virtual ~CastleConfig();

public:
	void Startup();
	void Shutdown();

	CommandExecutor& GetExecutor() const;
	CastleClient& GetCastleClient() const;

	void RegisterConfigChangedObserver(ConfigChangedObserver& observer, int init_version);

public:
	virtual int GetProperties(int version, Properties& properties, Clusters& clusters) = 0;

private:
	virtual CommandHandler* NewFetchCommand(ConfigChangedObserver& observer, int init_version) = 0;

public:
	static const int HEART_BEAT_PERIOD = 10;

private:
	CommandExecutor* m_pExecutor;
	CommandHandler* m_pFetchProducerConfig;
	CommandHandler* m_pFetchConsumerConfig;

	CastleClient* m_pCastleClient;
	CommandHandler* m_pFetchConfig;
	bool m_Running;
};

class ProducerImpl;
class ProducerConfig : public CastleConfig
{
public:
	ProducerConfig(std::string const& name_space, ProducerImpl& producer);
	virtual ~ProducerConfig();

public:
	virtual int GetProperties(int version, Properties& properties, Clusters& clusters);

private:
	virtual CommandHandler* NewFetchCommand(ConfigChangedObserver& observer, int init_version);

private:
	ProducerImpl& m_producer;

};


class ClusterConsumerImpl;
class ConsumerConfig : public CastleConfig
{
public:
	ConsumerConfig(std::string const& name_space, ClusterConsumerImpl& consumer);
	virtual ~ConsumerConfig();

public:
	virtual int GetProperties(int version, Properties& properties, Clusters& clusters);

private:
	virtual CommandHandler* NewFetchCommand(ConfigChangedObserver& observer, int init_version);

private:
	ClusterConsumerImpl& m_consumer;
};


}



#endif //__MAFKA_CASTLE_CONFIG_H__
