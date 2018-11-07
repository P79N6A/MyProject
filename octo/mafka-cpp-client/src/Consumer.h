#ifndef __MAFKA_CONSUMER_H__
#define __MAFKA_CONSUMER_H__

#include <string>
#include "MessageHandler.h"
#include "SharedPtr.h"

namespace mafka
{

class Consumer;
class ConsumerDeleter
{
public:
	void operator()(Consumer* p);
};
typedef SharedPtr<Consumer, ConsumerDeleter> ConsumerPtr;

class ClusterConsumerImpl;
class Consumer
{
public:
	void RecvMessage(MessageHandler& handler);

private:
	Consumer(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space);
	~Consumer();

	int Open();
	void Close();

private:
	friend class ConsumerDeleter;
	friend ConsumerPtr NewConsumer(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space);
	ClusterConsumerImpl* m_pImpl;
};

ConsumerPtr NewConsumer(std::string const& appkey, std::string const& topic, std::string const& consumer_group, std::string const& name_space);

}



#endif //__MAFKA_CONSUMER_H__
