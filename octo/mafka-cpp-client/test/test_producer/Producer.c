#include "mafka_capi.h"
#include "Log.h"
#include <stdio.h>     /* for printf */
#include <stdlib.h>    /* for exit */
#include <getopt.h>


void msgCallBack(void* arg, int error_code, int partition, const char* buf, int len) {
	if(!error_code) {
		
		printf("reciver msg:[%s] len:[%d] ,partition:[%d] failed\n", buf, len, partition);
		INFO("reciver msg:[%s] len:[%d] ,partition:[%d] failed", buf, len, partition);
	} else {
		printf("reciver msg:[%s] len:[%d] ,partition:[%d] successed\n", buf, len, partition);
		INFO("reciver msg:[%s] len:[%d] ,partition:[%d] successed", buf, len, partition);
	}
}



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
	//mafka::SetupLogger("./mafka_producer.log", "INFO");
	//handle_argv(argc, argv);	
	mq_set_logger("./mafka_producer.log", "INFO");
//	printf("nameSpace:[%s] , topic: [%s], appkey: [%s]\n", bgNameSpace, topic, appKey);


	const int MAX_COUNT = 500;
	char* num_str = (char*) malloc(200);
	int partitionNum = 0;
	int timeout = 1000;
	for(int j =0 ; j < 20; j ++) {	
		mq_producer_t* mq_producer_ptr = (mq_producer_t*)calloc(sizeof(mq_producer_t),sizeof(char));
		if(!mq_producer_ptr) {
			printf(" open failed\n");
		}

		int r = mq_producer_open(mq_producer_ptr, "test_yzq", "test_for_c_php_format", "common");
			memset(num_str , 0, 200);	
			partitionNum = 1 % 2;
			sprintf(num_str, "%d:%d:%s", 1, partitionNum, "hello");
		for(int i = 0; i < 200; ++i)
		{
		
			int ret = 0;
	//	if(mode != NULL && strcmp(mode, "async") == 0) {
			if(1) {	
				ret = mq_producer_send_async((*mq_producer_ptr), num_str, strlen(num_str), msgCallBack, 0);
			}else {
				ret = mq_producer_send((*mq_producer_ptr), num_str, strlen(num_str), 20000);
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
		mq_producer_close(mq_producer_ptr);
		free(mq_producer_ptr);
		sleep(3);
	}
	free(num_str);
	return 0;
}

