//
// Created by  on 2017/8/12.
//
#ifndef SG_AGENT2_CCOMMON_H
#define SG_AGENT2_CCOMMON_H
#define RCV_PORT 5267
#define RUN_SCRIPT_SUCESS   7     //0X01 or 0x02 or 0x03���������λ1��ʾ�ɹ�
#define LOCAL_PORT 5267
#define CRANE_SERVER_PORT 8383
#define BIT_INT 100000
#define MAX_SEND_BUFFER 1024
#define MAX_LOCAL_IP 16
#define MAX_LOCAL_MASK 16
#define MAX_IP_NUM 8
#define MAX_IP_LEN 64

#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<sys/socket.h>
#include<sys/types.h>
#include<sys/select.h>
#include<netinet/in.h>
#include <signal.h>
#include <fcntl.h>
#include <sstream>
#include <iostream>
#include <string>
#include <memory>
//#include <chrono>
#include <arpa/inet.h>
#include <string.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <unistd.h>
#include <map>
#include <set>
#include <string>
#include <vector>
#include <iostream>
#include <fstream>
#include <sstream>
#include <streambuf>
#include <algorithm>

#include <stdio.h>
#include <sys/types.h>
#include <ifaddrs.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/ioctl.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdlib.h>
#include <errno.h>
#include <assert.h>
#include <stdarg.h>
#include <sys/socket.h>
#include <sys/file.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/msg.h>
#include <sys/shm.h>
#include <sys/time.h>
#include <string.h>
#include <boost/shared_ptr.hpp>
#include <boost/lexical_cast.hpp>

#include <signal.h>
#include <sys/stat.h>
#include <unistd.h>
#include <execinfo.h>
#include <stdarg.h>
#include <sys/ipc.h>
enum EXE_CODE {
  ERROR_RETURN = -1,
  SUCESESS_RETURN

};

typedef enum LOADED_MODULE {
  INVALID_MODULE,
  CRANE_CLIENT_MODULE,
  EXTEND_CLIENT_MODULE

} LOADED_MODULE_TYPE;

enum SERVER_TYPE {
  INVALID_SERVER = -1,
  CRANE_TCP_SERVER,
  CRANE_UDP_SERVER,
  CRANE_HTTP_SERVER

};
enum SERVICE_TYPE {
  INVALID_SERVICE = -1,
  COMMAND_SERVICE,
  DEPLOY_SERVICE,
  LOG_SERVICE,
  EXTENT_SERVICE
};
enum KILL_TASK_TYPE{

  INVALID_KILL_TASK,
  SUCESS_KILL_TASK=5,
  ERROR_KILL_TASK=6,
  UNSURE_KILL_TASK
};
enum PROCESS_RETURN_TYPE{

  INVALID_RETURN_PROCESS=-1,
  SUCESS_RETURN_PROCESS=127,
  FAILED_RETURN_PROCESS

};
enum HEALTHY_CONDITION{

  INVALID_CONDITION=-1,
  IS_UNHELTHY=0,
  IS_HEALTHY=14  //2|4|8

};

enum SCHEDULE_RESULT{
  INVALID_SCH,
  FINISH_SCH=2,
  FAILED_SCH=4

};

enum RUN_STATE{
  UNFINISHED_STATE=0,
  FINISHED_STATE=1,
  FAILED_STATE=2


};
enum CPLUGIN_INIT{
  INIT_SUCESS=0,
  INIT_FAILED
};

typedef struct{
	    int nPid;
			std::string strTraceId;

}PidTrace_Info;





#endif //SG_AGENT2_CCOMMON_H




