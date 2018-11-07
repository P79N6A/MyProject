#ifndef __MAFKA_MOCK_CASTLE_CLIENT_H__
#define __MAFKA_MOCK_CASTLE_CLIENT_H__

#include <string>
#include <map>
#include <vector>

class BrokerInfo;
class CastleServiceClient;
namespace mafka
{

class ConfigFile;
class MockCastleClient
{
public:
	typedef std::map<std::string, std::string> Properties;
	typedef std::vector<BrokerInfo> Brokers;
	typedef std::vector<int> Partitions;

public:
	MockCastleClient();
	virtual ~MockCastleClient();

public:
	int GetProducerProperties(std::string const& producer_id, int version, std::string const& appkey, std::string const& topic,
			Properties& properties, Brokers& brokers, Partitions& partitions);

private:
	ConfigFile* m_pConfigFile;
};



}



#endif //__MAFKA_MOCK_CASTLE_CLIENT_H__