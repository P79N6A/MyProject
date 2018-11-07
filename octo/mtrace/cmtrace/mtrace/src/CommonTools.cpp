/**
* @file CommonTools.cpp
* @Brief 
* @Author tuyang@meituan.com
* @version 
* @Date 2015-01-14
*/

#include <iostream>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "CommonTools.h"
using namespace std;

int g_level = 0;
static const int offline_size = 4;
static std::string offline_list[offline_size] = {"10.4.246.240", "10.4.241.125", "10.4.241.165", "10.4.241.166"}; //线下product
static std::string localIp = getMachineIp();
const int SgAgentPort = 5266;
const bool SUCCESS = true;
const int SLEEPTIME = 5 * 1000; //5ms重新建立连接
const int SLOWQUERY_SEND_GAP = 10 * 1000 * 1000; //10 s
const int SLOWQUERY_LIST_SIZE = 100;
const int MAX_INTERFACE_NUM = 32;

string getHostName()
{
	char hname[128] = {0};
	struct hostent * hent;
	gethostname(hname, sizeof(hname));
	hent = gethostbyname(hname);

	if(hent)
	{
		return string(hent->h_name);
	}
	return string("");
}

string getMachineIp() {
    char ip[INET_ADDRSTRLEN] = {0};
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
    if (index>1) {
        int idx = 0;
        while(idx < index) {
            if( NULL != strstr(addrArray[idx], "10.")
               && 0 == strcmp(addrArray[idx], strstr(addrArray[idx], "10."))) {
                strcpy(ip, addrArray[idx]);
            }
            idx++;
        }
    }
    if (ifAddrStruct != NULL) {
        freeifaddrs(ifAddrStruct);
    }
        
    if ( std::string(ip).length() >0) {
        return std::string(ip);
    }
    
    //通过host方式获取IP
    std::string hIP = "";
    char hname[1024] = {0};
    gethostname(hname, sizeof(hname));
    struct hostent *hent;
    hent = gethostbyname(hname);
    if (NULL != hent) {
        hIP = inet_ntoa(*(struct in_addr*)(hent->h_addr_list[0]));
    } else {
        hIP = "127.0.0.1";
    }
    return hIP;
}


uint64_t getCurrentMilliTime()
{
	struct timeval timeVal;
	gettimeofday(&timeVal, NULL);
	uint64_t curTime = (timeVal.tv_sec * 1000 * 1000 + timeVal.tv_usec) / 1000; ///毫秒
	return curTime;
}

uint64_t getCurrentMicroTime()
{
	struct timeval timeVal;
	gettimeofday(&timeVal, NULL);
	uint64_t curTime = timeVal.tv_sec * 1000 * 1000 + timeVal.tv_usec; ///微秒
	return curTime;
}

std::string getSgAgentIp()
{
    std::string sgAgentIp;
    int pos = localIp.find("10");
    if(0 == pos) //10 打头的是线上机器或云主机
    {
        sgAgentIp = localIp;
    }
    else
    {
        unsigned int ts = time(NULL);
        int index = rand_r(&ts) % offline_size;
        sgAgentIp = offline_list[index];
    }
    return sgAgentIp;
}
