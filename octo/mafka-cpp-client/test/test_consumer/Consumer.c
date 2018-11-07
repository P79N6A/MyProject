#include "mafka_capi.h"
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

void callBack(void* args, int partition, const char* buf, int len) {
	printf("get message : [%s] ,len:[%d]\n",buf,len);
	usleep(2000);
}

int main(int argc, char* argv[])
{
	int ret = 0;
    mq_set_logger("mafka_cConsumerLog.log", "INFO");	

	for(int i =0 ; i< 100; i++) {
	//handle_argv(argc, argv);
	//printf("nameSpace:[%s] , topic: [%s], appkey: [%s] consumeTime:[%d]\n", bgNameSpace, topic, appKey, consumeTime);
		mq_consumer_t* mq_consumer_ptr = (mq_consumer_t*)calloc(sizeof(mq_consumer_t),sizeof(char));
		ret = mq_consumer_open(mq_consumer_ptr, "test_yzq", "test_for_php_commit", "test_group1", "test");
		printf("open ret :[%d]\n", ret);

		mq_consumer_recv_message((*mq_consumer_ptr), callBack, 0); 

		INFO("waiting\n");

		sleep(40);
		INFO("yzq--close start [%d]", i);	
		mq_consumer_close(mq_consumer_ptr);
		free(mq_consumer_ptr);
		INFO("yzq--close end [%d]", i);	
	}
	if(fp) {
		fclose(fp);
	}
	return 0;
}

