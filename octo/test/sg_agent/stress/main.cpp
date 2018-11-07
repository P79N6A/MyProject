#include <iostream>
#include "test_sg_agent.h"
#include <unistd.h>
#include <vector>

using namespace std;

void *fun(void *args)
{
    unsigned int tid = pthread_self();

    string host = "192.168.12.185";
    int port = 5266;
    std::string remoteAppkey = "com.sankuai.inf.chenxin11";
    SGAgentHandler client;
    client.init(remoteAppkey, host, port);

    std::vector<SGService> sgList;
    while(1)
    {
        try {
            client.getServiceList(sgList);
        } catch(TException &ex)
        {
            cout << ex.what() << endl;
            break;
        }
    }

    client.deinit();
    return (void *)0;
}

int main(int argc, char** argv)
{
    time_t start_time = time(NULL);
    int MAXSIZE = 0;
    for( int i = 0; i < argc; ++i)
    {
        MAXSIZE = atoi(argv[i]);
    }
    pthread_t tid[MAXSIZE+1];
    for(int i = 1; i <= MAXSIZE; i++)
    {
        int j = i;
        pthread_create(&tid[j], NULL, fun, &j);
    }

    for(int i = 1; i <= MAXSIZE; i++)
    {
        pthread_join(tid[i], NULL);
    }

    time_t end_time = time(NULL);

    return 0;
}
