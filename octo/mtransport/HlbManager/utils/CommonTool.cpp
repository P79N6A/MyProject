#include "CommonTool.h"
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <ifaddrs.h>
#include <sys/socket.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <string.h>
#include "Version.h"

const int MAX_INTERFACE_NUM = 32;

int parse_arg(int argc, char * argv[])
{
    int oc = 0;

    while ((oc = getopt(argc, argv, "v")) != -1)
    {
        switch(oc)
        {
            case 'v':
                printf("hlb_version=%s\n",HLB_VERSION);
                return -1;
                break;
            default:
                printf("usage -v to show version\n");
                break;
        }
    }
    return 0;
}

int socket_set_nonblock(int &fd)
{
    int ret;
    int flags = fcntl(fd, F_GETFD);
    if(-1 == flags)
    {
        return -1;
    }
    flags |= O_NONBLOCK;
    ret = fcntl(fd, F_SETFD);
    if(-1 == ret)
    {
        return -1;
    }
    return 0;
}

int socket_set_keepalive(int &fd)
{
    int alive = 1;
    /* 开启保活 */
    if(0 != setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &alive, sizeof(alive)))
    {
        return -1;
    }

    /* idle秒内无数据，则触发保活 */
    int idle = 5;
    if(0 != setsockopt(fd, SOL_TCP, TCP_KEEPIDLE, &idle, sizeof(idle)))
    {
        return -2;
    }

    /* 未收到响应，则interval之后继续发包 */
    int interval = 2;
    if(0 != setsockopt(fd, SOL_TCP, TCP_KEEPINTVL, &interval, sizeof(interval)))
    {
        return -3;
    }
    
    /* 连续cnt次都失败，则视为真正失败 */
    int cnt = 3;
    if(0 != setsockopt(fd, SOL_TCP, TCP_KEEPCNT, &cnt, sizeof(cnt)))
    {
        return -4;
    }
    
    return 0;
}

void split(const std::string& s, char c, std::vector<std::string>& v) 
{
    std::string::size_type i = 0;
    std::string::size_type j = s.find(c);
    
    while (std::string::npos != j) 
    {
        v.push_back(s.substr(i, j-i));
        i = ++j;
        j = s.find(c, j);

    }
    
    v.push_back(s.substr(i, s.length( )));
   
    return;
}

int getIntranet( std::string& retIP) {
    retIP = "127.0.0.1";
    int statusCode = 0;
    
    char ip[INET_ADDRSTRLEN];
    struct ifaddrs* ifAddrStruct = NULL;
    struct ifaddrs* ifa = NULL;
    void* tmpAddrPtr = NULL;
    char addrArray[MAX_INTERFACE_NUM][INET_ADDRSTRLEN];
    getifaddrs(&ifAddrStruct);
    int index = 0;
    for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa -> ifa_next)
    {
        if ((NULL == ifa->ifa_addr) || (0 == strcmp(ifa->ifa_name, "vnic")))
        {
            continue;
        }
        if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
            tmpAddrPtr = &((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
            inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
            if(0 == strcmp(addrArray[index], "127.0.0.1")) {
                continue;
            }
            
            strcpy(ip, addrArray[index]);
            if(++index > MAX_INTERFACE_NUM - 1) {
                break;
            }
            
        }
    }
    
    if(index > 1) {
        int idx = 0;
        while(idx < index) {
            if(NULL != strstr(addrArray[idx], "10.")
               && 0 == strcmp(addrArray[idx],
                              strstr(addrArray[idx], "10."))) {
                   strcpy(ip, addrArray[idx]);
               }
            idx++;
        }
    }
    else if (0 >= index){
        statusCode = -1;
    }
    
    if (ifAddrStruct != NULL) {
        freeifaddrs(ifAddrStruct);
    }
    
    retIP = std::string(ip);
    return statusCode;
}

