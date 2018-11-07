#ifndef __MAFKA_MAFKA_CAPI_H__
#define __MAFKA_MAFKA_CAPI_H__

#ifdef __cplusplus
extern "C" {
#endif

struct mq_producer_t
{
	void* impl;
};
typedef struct mq_producer_t mq_producer_t;
typedef void (*complete_callback)(void* args, int error_code, int partition, const char* buf, int len);
int mq_producer_open(mq_producer_t* producer, const char* appkey, const char* topic, const char* name_space);
void mq_producer_close(mq_producer_t* producer);
int mq_producer_send(mq_producer_t producer, const char* buf, int len, int timeout);
int mq_producer_send_partition(mq_producer_t producer,int partition, const char* buf, int len, int timeout);
int mq_producer_send_async(mq_producer_t producer, const char* buf, int len, complete_callback callback, void* args);
int mq_producer_is_closed(mq_producer_t producer);

struct mq_consumer_t
{
	void* impl;
	void* handler;
};
typedef struct mq_consumer_t mq_consumer_t;
typedef int (*message_callback)(void* args, int partition, const char* buf, int len);
int mq_consumer_open(mq_consumer_t* consumer, const char* appkey, const char* topic, const char* consumer_group, const char* name_space, int is_callback);
//int mq_consumer_open(mq_consumer_t* consumer, const char* appkey, const char* topic, const char* consumer_group, const char* name_space);
//int mq_consumer_pull_open(mq_consumer_t* consumer, const char* appkey, const char* topic, const char* consumer_group, const char* name_space);
void mq_consumer_close(mq_consumer_t* consumer);
void mq_consumer_recv_message(mq_consumer_t consumer, message_callback callback, void* args);
int mq_consumer_is_closed(mq_consumer_t consumer);
char* mq_consumer_get_message(mq_consumer_t* consumer, int timeout, int* len);
int mq_consumer_commit(mq_consumer_t* consumer);
int mq_echo_int(int n);
const char * mq_echo_string(const char * s);
void mq_set_logger(const char* filename, const char* level);



#ifdef __cplusplus
}
#endif




#endif //__MAFKA_MAFKA_CAPI_H__
