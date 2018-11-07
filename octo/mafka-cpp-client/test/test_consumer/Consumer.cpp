#include "Producer.h"
#include "CompleteHandler.h"

#include "Consumer.h"
#include "MessageHandler.h"

#include "StringConverter.h"
#include "Log.h"

char* appKey = NULL;
char* topic = NULL;
char* bgNameSpace = NULL;
char* messageContent = NULL;
char* group = NULL;
char* fileName = NULL;
FILE* fp = NULL;
int messageNum = 0;
int consumeTime = 600;

class RecvHandler : public mafka::MessageHandler
{
public:
	virtual mafka::ConsumeStatus OnRecvMessage(int partition, const char* buf, int len)
    {
		INFO("recv message=[%s], partition=[%d], len=[%d]\n", buf, partition, len);
		printf("recv message=[%s], partition=[%d], len=[%d]\n", buf, partition, len);
		if(fp) {
			fwrite(buf , sizeof(char), len, fp);
			fwrite("\n", sizeof(char), strlen("\n"), fp);
			fflush(fp);
		}
		return mafka::CONSUME_SUCCESS;
    }
};
RecvHandler recv_handler;



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
	group = (char*) calloc(sizeof(char), 100);
	fileName = (char*) calloc(sizeof(char), 100);
	messageContent = (char*) calloc(sizeof(char), 1000);
	
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
		} else if(strcmp(key, "group") == 0) {
			memcpy(group, value, strlen(value));
		} else if(strcmp(key, "filename") == 0) {
			memcpy(fileName, value, strlen(value));
			fp = fopen(fileName,"wb");
			if(!fp) {
				printf("open file error[%s] reason:[%s]\n", fileName, strerror(errno));
			} else {
				printf("open [%s] success reason:[%s]\n", fileName, strerror(errno));
			}
		} else if(strcmp(key, "consumeTime") == 0) {
			consumeTime = atoi(value);	
		}
	}
}


int main(int argc, char* argv[])
{
	mafka::SetupLogger("./mafka_consumer.log","INFO");
	
	handle_argv(argc, argv);
	printf("nameSpace:[%s] , topic: [%s], appkey: [%s] consumeTime:[%d]\n", bgNameSpace, topic, appKey, consumeTime);
	mafka::ConsumerPtr consumer = mafka::NewConsumer(appKey, topic, group, bgNameSpace);
	if(!consumer)
	{
		ERROR("consumer startup failed");
		return 0;
	}

	consumer->RecvMessage(recv_handler);

	INFO("waiting\n");

	sleep(10);
	if(fp) {
		fclose(fp);
	}
	
	
	return 0;
}

