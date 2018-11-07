#include <unistd.h>
#include <stdio.h>
#include <stdint.h>
#include <pthread.h>
#include "TraceInterface.h"
#include <sys/time.h>
#include <stdlib.h>

#define THREAD_NUM 10

void * callbackFun(void * arg)
{
    SERVER_INIT( "com.sankuai.hlb.rt", "unknownService");
    for (int i=0; i<1000; i++) {
        SET_DEBUG(1);
        SERVER_SEND_WITH_APPKEY_V2( "remoteAppkey-123", "172.21.6.65", 2345, "cmtraceTestCase");
        usleep(200);
		int clientStatus = 0;
	    SERVER_RECV(clientStatus);		
	}
    return (void *)0;
}

int main()
{
    CONFIG.setThreshold("com.sankuai.hlb.rt", 10);
    //CONFIG.setThreshold("remoteAppKey", "rpcName", 10);
    
    pthread_t tid[THREAD_NUM];
    for(int i = 0; i < THREAD_NUM; i++)
    {
        pthread_create(&tid[i], NULL, callbackFun, NULL);
    }

    for(int i = 0; i < THREAD_NUM; i++)
    {
        pthread_join(tid[i], NULL);
    }	
	return 0;
}
