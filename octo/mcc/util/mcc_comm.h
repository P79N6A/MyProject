#ifndef _MCC_INC_COMM_H_
#define _MCC_INC_COMM_H_

#include <stdio.h>
#include <sys/types.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <stdint.h>
#include <stdlib.h>

const int MAX_INTERFACE_COUNT = 3;

int getIntranet(char ip[INET_ADDRSTRLEN]) {
    struct ifaddrs* ifAddrStruct = NULL;
    struct ifaddrs* ifa = NULL;
    void* tmpAddrPtr = NULL;
    char addrArray[MAX_INTERFACE_COUNT][INET_ADDRSTRLEN];
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

            if(index > MAX_INTERFACE_COUNT - 1) {
                break;
            }

            index++;
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
        return ERR_COMM_NOTGETIP;
    }

    if (ifAddrStruct != NULL) {
         freeifaddrs(ifAddrStruct);
    }
    return 0;
}

void getHostInfo(char hostInfo[256], char ip[INET_ADDRSTRLEN]) {
    FILE * fp;
    char hostCMD[64];
    strncpy(hostCMD, "host ", 5);
    strncpy(hostCMD + 5, ip, INET_ADDRSTRLEN);
    fp = popen(hostCMD ,"r");
    fgets(hostInfo, 256,fp);
    pclose(fp);
    fp = NULL;
}

void getInterfaceIp(char ip[INET_ADDRSTRLEN])
{
    struct ifaddrs *ifAddrStruct = NULL;
    struct ifaddrs *ifa = NULL;
    void *tmpAddrPtr = NULL;
    char addrArray[MAX_INTERFACE_COUNT][INET_ADDRSTRLEN];
    getifaddrs(&ifAddrStruct);
    int index = 0;
    for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) 
    {
        if ((NULL == ifa->ifa_addr) || (0 == strcmp(ifa->ifa_name, "vnic")))
        {
            continue;
        }
        if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
            tmpAddrPtr = &((struct sockaddr_in *)ifa->ifa_addr)->sin_addr;
            inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
            if(0 == strcmp(addrArray[index], "127.0.0.1") )
                continue;
            strcpy(ip, addrArray[index]);

            if(index > MAX_INTERFACE_COUNT - 1) break;

            index ++;
        }
    }
    if(index > 1)
    {
        int idx = 0;
        while(idx < index) 
        {
            if(NULL != strstr(addrArray[idx], "10.") && 0 == strcmp(addrArray[idx], strstr(addrArray[idx], "10.")) ) {
                strcpy(ip, addrArray[idx]);
            }
            idx ++;
        }
    }
    if (ifAddrStruct!=NULL)
         freeifaddrs(ifAddrStruct);
    return;
}

#endif
