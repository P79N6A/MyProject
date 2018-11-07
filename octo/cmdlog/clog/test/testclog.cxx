#include "../include/clog/log.h"
#include <unistd.h>
#include <string>
#include <sstream>
#include <pthread.h>
#include <iostream>
#include <stdlib.h>
#include <sys/time.h>
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h>
#include <stdio.h>

namespace {
    int NUM_THREADS = 0;// thread numbers
    int LOOP = 0;
    int TYPE = 0;
    std::string log = "1234567890";
}

void usage(int argc, char **argv) {
    printf("Usage:\n");
    printf("    %s ./config/log.conf [-t] 1 [-n] 1 [-T] 0\n", argv[0]);
    printf("Options:\n");
    printf("    -t    thread number\n");
    printf("    -n    loop nums per thread\n");
    printf("    -T    test type; 1 : test_single_log/ 2 : test_common_log / 3 : test_tag_log \n");
}

unsigned long long TimeTap(struct timeval timeStart, struct timeval timeEnd) {
    unsigned long long timeTap = 0;
    timeTap = 1000000*(timeEnd.tv_sec - timeStart.tv_sec) + (timeEnd.tv_usec - timeStart.tv_usec);
    return timeTap;
}

void test_common_log()
{
    char *file_path_getcwd;
    file_path_getcwd=(char *)malloc(80);
    getcwd(file_path_getcwd,80);
    // test C printf format
    CLOG_DEBUG("haha [debug]%s",file_path_getcwd);
    CLOG_INFO("haha [info]%s",file_path_getcwd);
    CLOG_WARN("haha [warn]%s",file_path_getcwd);
    CLOG_ERROR("haha [error]%s",file_path_getcwd);

    // test C++ stream format
    CLOG_STR_DEBUG("[debug] str" << file_path_getcwd);
    CLOG_STR_INFO("[info] str" << file_path_getcwd);
    CLOG_STR_WARN("[warn] str" << file_path_getcwd);
    CLOG_STR_ERROR("[error] str" << file_path_getcwd);

    for (int j=0;j<9;j++){
        log += "abcdefghij"; 
    }

    CLOG_STR_DEBUG(log);
    CLOG_STR_INFO(log);
    CLOG_STR_WARN(log);
    CLOG_STR_ERROR(log);

    CLOG_ERROR("haha %s%s%d",log.c_str(),"1",2);
    CLOG_WARN("haha %s%s%d",log.c_str(),"1",2);
    CLOG_INFO("haha %s%s%d",log.c_str(),"1",2);
    CLOG_DEBUG("haha %s%s%d",log.c_str(),"1",2);
}

void test_single_log(int num = 0)
{
    int pid = getpid();
    CLOG_STR_ERROR("pid is " << pid << " threadId is " << "[" << pthread_self() << "]" << "type " << TYPE << " num " << num);
}

void test_tag_log()
{        //test put tags
    std::map<std::string, std::string> test_map;
    std::string k1 = "key1";
    std::string k2 = "key2";
    std::string k3 = "traceID";
    std::string value = "test";

    CLOG_DEBUG("haha %s%s%d",SUBCATEGORY(log).c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_INFO("haha %s%s%d",SUBCATEGORY(log).c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_WARN("haha %s%s%d",SUBCATEGORY(log).c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_ERROR("haha %s%s%d",SUBCATEGORY(log).c_str(),PUTTAGS(test_map).c_str(),"1",2);

    test_map[k1] = value;
    test_map[k2] = value;
    std::string str = "!";

    CLOG_DEBUG("haha %s%s%d",SUBCATEGORY("log").c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_INFO("haha %s%s%d",SUBCATEGORY("log").c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_WARN("haha %s%s%d",SUBCATEGORY("log").c_str(),PUTTAGS(test_map).c_str(),"1",2);
    CLOG_ERROR("haha %s%s%d",SUBCATEGORY("log").c_str(),PUTTAGS(test_map).c_str(),"1",2);
    //test user-defined traceID
    test_map[k3] = "ddddd";
    std::string mes = " message";
    CLOG_STR_DEBUG(PUTTAGS(test_map, mes) << " hello world " << str);
    CLOG_STR_INFO(PUTTAGS(test_map, mes) << " hello world " << str);
    CLOG_STR_WARN(PUTTAGS(test_map, mes) << " hello world " << str);
    CLOG_STR_ERROR(PUTTAGS(test_map, mes) << " hello world " << str);
}

void* test_log( void* args )  
{  
	std::cout <<  "test thread [" << pthread_self() << "]" << "type " << TYPE <<  std::endl;
	int num = LOOP;
	while(num > 0) {
        switch(TYPE){
            case 1:
                test_single_log(num);
                break;
            case 2:
                test_common_log();
                break;
            case 3:
                test_tag_log();
                break;
            default:
                test_common_log();
                test_tag_log();
        }
        num-- ;
    }
    pthread_exit(NULL);
} 

int main(int argc, char *argv[])
{
    for(int i=1; i<argc; i++) {
        if (strcmp(argv[i],"-t") == 0) {
            if (i+1 < argc) {
                NUM_THREADS = atoi(argv[i+1]);
                i++;
                continue;
            }    
        } else if (strcmp(argv[i],"-n") == 0 ) {
            if (i+1 < argc) {
                LOOP = atoi(argv[i+1]);
                i++;
                continue;
            }    
        } else if (strcmp(argv[i],"-T") == 0) {
            if (i+1 < argc) {
                TYPE = atoi(argv[i+1]);
                i++;
                continue;
            }    
        }
    }

	if (NUM_THREADS <= 0 || LOOP <= 0) {
        usage(argc, argv);
        exit(1);
    }	

    test_single_log();
    CLOG_INIT("/opt/logssss");
    test_single_log();
    CLOG_INIT("./config/log.conf");
    test_single_log();
    CLOG_INIT("/opt/logll");
    struct timeval TimePosStart = {0};
    struct timeval TimePosEnd = {0};
    gettimeofday(&TimePosStart, NULL);
	pthread_t tids[NUM_THREADS]; //线程id  
    int ret;
    for( int i = 0; i < NUM_THREADS; ++i )  
    {  
         ret = pthread_create( &tids[i], NULL, test_log, NULL); 
        if( ret != 0 ) 
        {  
            std::cout << "pthread_create error:error_code=" << ret << std::endl;  
        }  
	}

	for(int i = 0; i < NUM_THREADS; ++i)
	{
		ret = pthread_join(tids[i], NULL);
	} 

    CLOG_CLOSE();

    gettimeofday(&TimePosEnd, NULL);
    unsigned long long tamp = TimeTap(TimePosStart,TimePosEnd);      
    unsigned long long tamp1 = tamp /1000;
    unsigned long long qps1 = static_cast<unsigned long long>(NUM_THREADS) * LOOP* 1000;
    unsigned long long qps = 0;
    if (tamp1>0) qps = qps1 / tamp1;
    std::cout << "time consume: " << tamp << " thread " << NUM_THREADS << " " << qps<< std::endl;

	std::cout << "finish this process" << std::endl;
	return 0;
}


