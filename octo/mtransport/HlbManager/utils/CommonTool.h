#ifndef __HLB_COMMON_TOOL_H__
#define __HLB_COMMON_TOOL_H__

#include <sys/time.h>
#include <stdio.h>
#include <string>
#include <vector>

#define SAFE_DELETE(p) { if(p) { delete (p); (p)=NULL; } }

#define START_TIME struct timeval tv_begin, tv_end;\
            gettimeofday(&tv_begin, NULL);

#define END_TIME(funName) gettimeofday(&tv_end, NULL);\
            LOG_STAT("PERFORM: " << funName << ", used time : " << (tv_end.tv_sec -  tv_begin.tv_sec) * 1000 * 1000 + (tv_end.tv_usec - tv_begin.tv_usec) << " us");

int parse_arg(int argc, char * argv[]);

int socket_set_nonblock(int &fd);

int socket_set_keepalive(int &fd);

void split(const std::string& s, char c, std::vector<std::string>& v);

//获取本地ip
//return:  0  正确获取
//        -1  获取失败，将返回"127.0.0.1"
int getIntranet( std::string& retIP);

#endif
