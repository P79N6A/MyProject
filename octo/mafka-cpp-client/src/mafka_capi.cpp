#include "mafka_capi.h"
#include "ProducerImpl.h"
#include "ClusterConsumerImpl.h"

#include "CompleteHandler.h"
#include "MessageHandler.h"

#include "Log.h"

using namespace mafka;

/*
struct mq_producer_t
{
	ProducerImpl* impl;
};
*/

int mq_producer_open(mq_producer_t* producer, const char* appkey, const char* topic, const char* name_space)
{
	if(!producer)
		return RD_KAFKA_RESP_ERR__FAIL;

	if(producer->impl)
		return 0;

	ProducerImpl* impl = new ProducerImpl(appkey, topic, name_space);
	int errorcode = impl->Open();
	impl->Startup();

	if(errorcode)
	{
		impl->Cancel();
		impl->Shutdown();
		impl->Close();
		return errorcode;
	}

	producer->impl = impl;

	return 0;
}

void mq_producer_close(mq_producer_t* producer)
{
	if(!producer)
		return;

	ProducerImpl* impl = static_cast<ProducerImpl*>(producer->impl);
	if(!impl)
		return;
	
	//异步发送
	impl->Cancel();
	impl->Shutdown();
	impl->Close();

	producer->impl = NULL;
}

int mq_producer_send(mq_producer_t producer, const char* buf, int len, int timeout)
{
	ProducerImpl* impl = static_cast<ProducerImpl*>(producer.impl);
	if(!impl)
		return RD_KAFKA_RESP_ERR__FAIL;

	return impl->SendMessage(buf, len, timeout);
}

int mq_producer_send_partition(mq_producer_t producer, int partition, const char* buf, int len, int timeout)
{
        ProducerImpl* impl = static_cast<ProducerImpl*>(producer.impl);
        if(!impl)
                return RD_KAFKA_RESP_ERR__FAIL;

        return impl->SendMessage(partition, buf, len, timeout);
}

class SendAsyncHandler : public CompletionHandler
{
public:
	SendAsyncHandler(complete_callback callback, void* args)
	:m_callback(callback), m_args(args)
	{

	}

    virtual void OnComplete(int error_code, int partition, const char* buf, int len)
    {
		m_callback(m_args, error_code, partition, buf, len);

		delete this;
    }

private:
	complete_callback m_callback;
	void* m_args;
};
int mq_producer_send_async(mq_producer_t producer, const char* buf, int len, complete_callback callback, void* args)
{
	ProducerImpl* impl = static_cast<ProducerImpl*>(producer.impl);
	if(!impl)
		return RD_KAFKA_RESP_ERR__FAIL;

	return impl->SendAsyncMessage(buf, len, *new SendAsyncHandler(callback, args));
}

int mq_producer_is_closed(mq_producer_t producer)
{
	return producer.impl == NULL;
}


class RecvMessageHandler : public mafka::MessageHandler
{
public:

	RecvMessageHandler(message_callback callback, void* args)
	:m_callback(callback), m_args(args)
	{

	}

	~RecvMessageHandler()
	{
		free(m_args);
	}

	virtual mafka::ConsumeStatus OnRecvMessage(int partition, const char* buf, int len)
    {
		return (mafka::ConsumeStatus) m_callback(m_args, partition, buf, len);
    }

private:
	message_callback m_callback;
	void* m_args;
};

/*
struct mq_consumer_t
{
	ClusterConsumerImpl* impl;
	RecvMessageHandler* handler;
};
*/
int mq_consumer_open(mq_consumer_t* consumer, const char* appkey, const char* topic, const char* consumer_group, const char* name_space, int is_callback)
{
	if(!consumer)
		return RD_KAFKA_RESP_ERR__FAIL;

	if(consumer->impl)
		return 0;

	ClusterConsumerImpl* impl = new ClusterConsumerImpl(appkey, topic, consumer_group, name_space);
	bool mode = is_callback != 0 ? true : false;
	int errorcode = impl->Open(mode);
	impl->Startup();

	if(errorcode)
	{
		impl->Close();
		impl->Shutdown();
		delete impl;
		return errorcode;
	}

	consumer->impl = impl;
	consumer->handler = NULL;

	return 0;

}

void mq_consumer_close(mq_consumer_t* consumer)
{
	if(!consumer)
		return;

	ClusterConsumerImpl* impl = static_cast<ClusterConsumerImpl*>(consumer->impl);
	if(!impl)
		return;

	impl->Close();
	impl->Shutdown();

	delete impl;
	consumer->impl = NULL;

	RecvMessageHandler* handler = static_cast<RecvMessageHandler*>(consumer->handler);
	delete handler;
	consumer->handler = NULL;
}

void mq_consumer_recv_message(mq_consumer_t consumer, message_callback callback, void* args)
{
	ClusterConsumerImpl* impl = static_cast<ClusterConsumerImpl*>(consumer.impl);
	if(!impl)
		return;

	if(consumer.handler)
		return;

	RecvMessageHandler* handler = new RecvMessageHandler(callback, args);
	consumer.handler = handler;
	impl->RecvMessage(*handler);
}

int mq_consumer_is_closed(mq_consumer_t consumer)
{
	return consumer.handler == NULL;
}

char* mq_consumer_get_message(mq_consumer_t* consumer, int timeout, int* len)
{
	if(consumer == NULL || len == NULL) 
                return NULL;

        ClusterConsumerImpl* impl = static_cast<ClusterConsumerImpl*>(consumer->impl);
        if(!impl)
                return NULL;
	char* msg = impl->GetMessage(timeout, *len);
	return msg;
}

int mq_consumer_commit(mq_consumer_t* consumer)
{
        if(!consumer)
                return 0;

        ClusterConsumerImpl* impl = static_cast<ClusterConsumerImpl*>(consumer->impl);
        if(!impl)
                return 0;

        return impl->Commit();
}
int mq_echo_int(int n)
{
    return n;
}

const char * mq_echo_string(const char * s)
{
    return s;
}

void mq_set_logger(const char* filename, const char* level)
{
	SetupLogger(filename, level);
}


