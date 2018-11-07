#ifndef __MAFKA_PRODUCER_H__
#define __MAFKA_PRODUCER_H__

#include <string>
#include "CompleteHandler.h"
#include "SharedPtr.h"

namespace mafka
{

class Producer;
class ProducerDeleter
{
public:
	void operator()(Producer* p);
};
typedef SharedPtr<Producer, ProducerDeleter> ProducerPtr;

class ProducerImpl;
class Producer
{
public:
	int Send(const char* buf, int len, int timeout);
	int SendMessage(const char* buf, int len, int timeout);
	int SendAsync(const char* buf, int len, CompletionHandler& handler);
	int SendAsyncMessage(const char* buf, int len, CompletionHandler& handler);

	int Send(int partition, const char* buf, int len, int timeout);
	int SendMessage(int partition, const char* buf, int len, int timeout);
	int SendAsync(int partition, const char* buf, int len, CompletionHandler& handler);
	int SendAsyncMessage(int partition, const char* buf, int len, CompletionHandler& handler);
	void SetPollTimeout(int timeout);
	void ForbidSerializable();

private:
	Producer(std::string const& appkey, std::string const& topic, std::string const& name_space);
	~Producer();

	int Open();
	void Close();

private:
	friend class ProducerDeleter;
	friend ProducerPtr NewProducer(std::string const& appkey, std::string const& topic, std::string const& name_space, bool low_load);
	ProducerImpl* m_pImpl;
};

ProducerPtr NewProducer(std::string const& appkey, std::string const& topic, std::string const& name_space, bool low_load = false);

}



#endif //__MAFKA_PRODUCER_H__
