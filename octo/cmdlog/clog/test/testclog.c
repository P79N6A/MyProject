#include <clog/agentclog.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>

int NUM_THREADS = 1;// thread numbers
int LOOP = 1;
#define CONFIG_PATH "./log.conf"
unsigned long long TimeTap(struct timeval timeStart, struct timeval timeEnd){
    unsigned long long timeTap = 0;
    timeTap = 1000000*(timeEnd.tv_sec - timeStart.tv_sec) + (timeEnd.tv_usec - timeStart.tv_usec);
    return timeTap;
}
void* test_log( void* args ){
    void *handle = args;
    char tags[1024] = {0};
    char subcategory[100] = {0};
    int i=0;
	int ret = -1;
	
    putTags(tags,"a=b b=c");
    subCategory(subcategory,"com.sankuai.pic_data"); 

    for(i=0;i<LOOP;i++){
	LOG_ERROR(handle,"%s %d","test log",10); 
	LOG_WARN(handle,"%s %d","test log",10);
	LOG_INFO(handle,"%s %d","test log",10);
	LOG_DEBUG(handle,"%s %d","test log",10);
	// put tag
	LOG_ERROR(handle,"%s %s",tags,"hah");

	//print log for subcategory 
	LOG_ERROR(handle,"%s %s",subcategory,"hah");
	LOG_INFO(handle,"%s %s",subcategory,"hah");
	//put tag for subcategory
	LOG_ERROR(handle,"%s %s %s",subcategory,tags,"hah");
    }
}

int main(int argc, char **argv)
{
	void *handle = NULL;
	
    handle = open_clog(CONFIG_PATH);
    //handle = open_cmdlog("");
	if (!handle){
		fprintf(stderr, "get cmdlog err!\n");
		return -1;
	}
	if (argc > 2){
		NUM_THREADS = atoi(argv[1]);
		LOOP = atoi(argv[2]);
	}	
    pthread_t tids[NUM_THREADS]; //线程id
    int ret;
    int i=0;
    struct timeval TimePosStart = {0};
    struct timeval TimePosEnd = {0};
    for( i = 0; i < NUM_THREADS; ++i )    
    {
        ret = pthread_create( &tids[i], NULL, test_log, handle);
        if( ret != 0 ){}
    }
    for(i = 0; i < NUM_THREADS; ++i)
    {
        ret = pthread_join(tids[i], NULL);
    }
    close_clog(handle);
    gettimeofday(&TimePosEnd, NULL);
    unsigned long long tamp = TimeTap(TimePosStart,TimePosEnd);
    unsigned long long tamp1 = tamp /1000;
    unsigned long long qps1 = (unsigned long long)NUM_THREADS * LOOP * 1000;
    unsigned long long qps = qps1 / tamp1;
    printf("time %llu thread %d qps %llu \n",tamp,NUM_THREADS,qps);
    return 0;
}
