//
// Created by honey on 2017/8/19.
//
#include "cJSON.h"
char* Command2Json()
{
    cJSON *root;
    char* cmdOut;
    char* sr
    root = cJSON_CreateObject();
    cJSON_AddItemToObject(root, "type", cJSON_CreateString("schdule"));
    cJSON_AddItemToObject(root, "taskName", cJSON_CreateString("runTomcat"));
    cJSON_AddItemToObject(root, "jobUniqueCode", cJSON_CreateString("meituan"));
    cJSON_AddItemToObject(root, "traceId", cJSON_CreateString("20171000-1000"));
    cJSON_AddItemToObject(root, "runState", cJSON_CreateString("0"));
    cmdOut = cJSON_Print(root);
    srcjson = cmdOut;
    SAFE_FREE(out);
    cJSON_Delete(root);
    return srcjson;
}

int main(int argc,char **argv)
{
    struct sockaddr_in sin;
    char buf[MAX_LINE];
    int s_fd;
    int port = 5267;
    char *str=Command2Json();

    char *serverIP = "127.0.0.1";
    int n;
    if(argc > 1)
    {
        str = argv[1];
    }

    bzero(&sin , sizeof(sin));

    sin.sin_family = AF_INET;
    inet_pton(AF_INET,serverIP,(void *)&sin.sin_addr);
    sin.sin_port = htons(port);

    if((s_fd = socket(AF_INET,SOCK_STREAM,0)) == -1)
    {
        perror("fail to create socket");
        exit(1);
    }
    if(connect(s_fd,(struct sockaddr *)&sin,sizeof(sin)) == -1)
    {
        perror("fail to create socket");
        exit(1);
    }
	string test;
	std::tostring();
    n = send(s_fd, str , strlen(str) + 1, 0);
    if(n == -1)
    {
        perror("fail to send");
        exit(1);
    }

    n = recv(s_fd ,buf , MAX_LINE, 0);
    if(n == -1)
    {
        perror("fail to recv");
        exit(1);
    }
    printf("the length of str = %s\n" , buf);
    if(close(s_fd) == -1)
    {
        perror("fail to close");
        exit(1);
    }
    return 0;
}