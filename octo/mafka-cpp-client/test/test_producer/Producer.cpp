#include "Producer.h"
#include "CompleteHandler.h"
#include "Log.h"
#include <stdio.h>     /* for printf */
#include <stdlib.h>    /* for exit */
#include <getopt.h>

class TestHandler : public mafka::CompletionHandler
{
	public:
		virtual void OnComplete(int error_code, int partition, const char* buf, int len)
		{
			if(error_code)
			{
				INFO("send complete error_code=%d\n", error_code);
			} else {
				INFO("send complete content:[%s] partition=[%d] len=[%d] ok\n", buf, partition, len);
			}
		}
};

TestHandler handler;

char* appKey = NULL;
char* topic = NULL;
char* bgNameSpace = NULL;
char* messageContent = NULL;
char* mode = NULL;
int messageNum = 0;


void getValueAndKey(char* content, char* key, char* value) {
	char* index = strstr(content,"=");
	memcpy(key, content, index-content);
	memcpy(value, index+1, strlen(content) - (index+1-content));
}

void handle_argv(int argc, char* argv[]) {
	char key[100] = {0};
	char value[100] = {0};
	appKey = (char*) calloc(sizeof(char), 100);
	topic = (char*) calloc(sizeof(char), 100);
	bgNameSpace = (char*) calloc(sizeof(char), 100);
	messageContent = (char*) calloc(sizeof(char), 1000);
	mode = (char*) calloc(sizeof(char), 100);
	
	for(int i = 1; i < argc ; i++ ) {
		memset(key, 0, 100);
		memset(value, 0, 100);
		getValueAndKey(argv[i], key, value);
		if (strcmp(key, "namespace") == 0) {	
			memcpy(bgNameSpace, value, strlen(value));
		} else if(strcmp(key, "appkey") == 0) {
			memcpy(appKey, value, strlen(value));
		} else if(strcmp(key, "topic") == 0) {
			memcpy(topic, value, strlen(value));
		} else if(strcmp(key, "count") == 0) {
			messageNum = atoi(value);
		} else if(strcmp(key, "message") == 0) {
			memcpy(messageContent, value, strlen(value));
		} else if(strcmp(key, "mode") == 0) {
			memcpy(mode, value, strlen(value));
		}
	}
}

int main(int argc, char* argv[])
{
	mafka::SetupLogger("./mafka_producer.log", "INFO");
	handle_argv(argc, argv);	
	printf("nameSpace:[%s] , topic: [%s], appkey: [%s]\n", bgNameSpace, topic, appKey);


	const int MAX_COUNT = 500;
	char* num_str = (char*) malloc(200);
	int partitionNum = 0;
	int timeout = 1000;
	mafka::ProducerPtr producer = mafka::NewProducer(appKey, topic, bgNameSpace);
	for(int i = 0; i < 1000000; ++i)
	{
		if(!producer)
		{
			ERROR("producer startup failed");
			return 0;
		}
		memset(num_str , 0, 200);	
		partitionNum = i % 6;
		sprintf(num_str, "%d:%d:%s", i, partitionNum, messageContent);
		
		int ret = 0;
	//	if(mode != NULL && strcmp(mode, "async") == 0) {
		if(1) {	
		ret = producer->SendAsync(partitionNum, num_str, strlen(num_str), handler); 
		} else {
			ret = producer->Send(partitionNum, num_str, strlen(num_str), timeout);
		}
		if(ret)
		{
			ERROR("send failed, ret=%d, i=%d, partitionNo:[%d] content=%s\n", ret, i, partitionNum,num_str);
			printf("send failed, ret=%d, i=%d, partitionNo:[%d] content=%s\n", ret, i, partitionNum,num_str);
		} else {
			INFO("send content: [%s] len: [%d] partitionNo: [%d]\n", num_str, strlen(num_str), partitionNum);
			printf("send ok: [%s] len: [%d] partitionNo: [%d]\n", num_str, strlen(num_str), partitionNum);
		}
		//sleep(1);
	}

	free(num_str);
	sleep(3);
	return 0;
}

