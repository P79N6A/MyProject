#ifndef __MAFKA_END_POINT_IMPL_H__
#define __MAFKA_END_POINT_IMPL_H__

#include <string>
#include <map>

#include "SharedPtr.h"

#include <rdkafka.h>

#include "CastleConfig.h"

namespace mafka
{

class CommandHandler;
class CommandExecutor;
class ConfigFile;
class Mutex;
typedef rd_kafka_resp_err_t ErrorCode;
class EndPointImpl
{

public:
	EndPointImpl(std::string const& appkey, std::string const& topic);
	virtual ~EndPointImpl();

public:
	std::string const& GetAppKey() const;
	std::string const& GetTopic() const;
	
	CommandExecutor& GetExecutor() const;

public:
	void Startup();
	void Shutdown();
	void SetPollTimeout(int timeout);

public:
	ErrorCode DoOpen(Properties const& properties, Brokers const& brokers, Partitions const& partitions);
	void DoClose(bool async = false);
	void DoRestart(Properties const& properties, Brokers const& brokers, Partitions const& partitions);

	void FixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions);
	void FilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties);

	bool WaitingMessageDoneAndRestart(Properties const& properties, Brokers const& brokers, Partitions const& partitions);

protected:
	bool CheckRestarting() const;
	Mutex& GetRestartMutex() const;

private:
	friend class CloseCommand;
	void Release();

private:
	virtual ErrorCode OnOPen(Properties const& global_properties, Properties const& topic_properties) = 0;
	virtual void OnClose() = 0;

	virtual void OnFixProperties(Properties& properties, Brokers const& brokers, Partitions const& partitions) = 0;
	virtual void OnFilterProperties(Properties const& properties, Properties& global_properties, Properties& topic_properties) = 0;

	virtual bool CheckAllMessageDone() const = 0;

private:
	void DumpProperties(Properties const& global_properties, Properties const& topic_properties);

private:
	std::string m_appkey;
	std::string m_topic;

	CommandExecutor* m_pExecutor;

	Mutex* m_pRestartMutex;
	bool m_restarting;
	bool m_Running;
	int m_poll_timeout;
};

typedef SharedPtr<EndPointImpl> EndPointPtr;


}



#endif //__MAFKA_END_POINT_IMPL_H__
