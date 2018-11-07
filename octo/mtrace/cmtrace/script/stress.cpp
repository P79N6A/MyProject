#include <unistd.h>
#include <stdio.h>
#include <stdint.h>
#include <pthread.h>
#include <TraceInterface.h>
#include <sys/time.h>
#include <stdlib.h>

#define THREAD_NUM 10

void * callbackFun(void * arg)
{
    int i = *(int *)arg;
    char localAppKey[1024];
    snprintf(localAppKey, sizeof(localAppKey), "localAppKey %d", i);

    char remoteAppKey[1024];
    snprintf(remoteAppKey, sizeof(remoteAppKey), "remoteAppKey %d", i);
	
    char buf[100] = {0};
    snprintf(buf, sizeof(buf), "hello world %d", i);
    while(1)
	{
        SERVER_INIT(localAppKey, remoteAppKey);
		SERVER_SEND("192.168.0.1:8080", buf);
        usleep(200);
		int clientStatus = 0;
	    SERVER_RECV(clientStatus);		
	}
    return (void *)0;
}

int main()
{
    pthread_t tid[THREAD_NUM];
    for(int i = 0; i < THREAD_NUM; i++)
    {
        int j = i;
        char buf[100] = {0};
        snprintf(buf, sizeof(buf), "hello world %d", j);

        char remoteAppKey[1024];
        snprintf(remoteAppKey, sizeof(remoteAppKey), "remoteAppKey %d", j);
        CONFIG.setThreshold(remoteAppKey, 0);
        pthread_create(&tid[i], NULL, callbackFun, (void *)&j);
    }

    for(int i = 0; i < THREAD_NUM; i++)
    {
        pthread_join(tid[i], NULL);
    }
	
	return 0;
}
