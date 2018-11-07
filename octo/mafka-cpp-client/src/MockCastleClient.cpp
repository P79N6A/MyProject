#include "MockCastleClient.h"
#include "ConfigFile.h"
#include "StringUtil.h"
#include "StringConverter.h"

#include "Log.h"

#include <castle/gen-cpp/CastleService.h>


namespace mafka
{

static const std::string castle_file_name("/tmp/castle.config");

MockCastleClient::MockCastleClient()
:m_pConfigFile(new ConfigFile())
{
	bool success = m_pConfigFile->LoadFromFile(castle_file_name);
	if(!success)
	{
		ERROR("load file failed, file=%s\n", castle_file_name.c_str());
	}
}

MockCastleClient::~MockCastleClient()
{
	delete m_pConfigFile;
}

int MockCastleClient::GetProducerProperties(std::string const& producer_id, int version, std::string const& appkey, std::string const& topic,
	Properties& properties, Brokers& brokers, Partitions& partitions)
{
	int new_version = version+1;

	ConfigSection const& section_properties = m_pConfigFile->GetSetion("properties");
	section_properties.GetAll(properties);

	ConfigSection const& section_borkers = m_pConfigFile->GetSetion("brokers");
	std::string broker_list;
	section_borkers.GetString("metadata.broker.list", broker_list);
	{
		size_t start = 0;
		size_t end = broker_list.find(',', start);
		while(true)
		{
			std::string address = broker_list.substr(start, end);
			size_t pos = address.find(':');
			std::string str_ip = address.substr(0, pos);
			StringUtil::TrimString(str_ip);
			std::string str_port = address.substr(pos+1);
			StringUtil::TrimString(str_port);

			BrokerInfo info;
			info.host = str_ip;
			info.port = StringConverter::ParseInt(str_port);
			brokers.push_back(info);

			if(end == std::string::npos)
				break;

			start = end+1;
			end = broker_list.find(',', start);
		}
	}
	ConfigSection const& section_partitions = m_pConfigFile->GetSetion("partitions");
	std::string partition_list;
	section_partitions.GetString("partitions", partition_list);
	{
		size_t start = 0;
		size_t end = broker_list.find(',', start);
		while(true)
		{
			std::string partition = broker_list.substr(start, end);
			partitions.push_back(StringConverter::ParseInt(partition));

			if(end == std::string::npos)
				break;
			start = end+1;
			end = broker_list.find(',', start);
		}
	}

	return new_version;
}

}

