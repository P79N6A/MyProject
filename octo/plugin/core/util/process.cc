#include "process.h"

#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/resource.h>
#include <glog/logging.h>
#include <fstream>
#include <iostream>
#include <fstream>
#include <iostream>
#include <boost/algorithm/string.hpp>

extern char **environ;

namespace cplugin {

static char *arg_start;
static char *arg_end;
static char *env_start;

void InitProcTitle(int argc, char** argv) {
  int i;
  arg_start = argv[0];
  arg_end = argv[argc-1] + strlen(argv[argc-1])+1;
  env_start = environ[0];
  for(i=0; i<argc; i++)
    argv[i] = strdup(argv[i]);
}

void SetProcTitle(const char* title) {
  int tlen = strlen(title)+1;
  int i;
  char *p;
           
  if(arg_end - arg_start < tlen && env_start == arg_end) {
    char *env_end = env_start;
    for(i=0; environ[i]; i++) {
      if(env_end == environ[i]) {
        env_end = environ[i] + strlen(environ[i]) + 1;
        environ[i] = strdup(environ[i]);
      } else {
        break;
      }
    }
    arg_end = env_end;
    env_start = NULL;
  }
  i = arg_end - arg_start;
  if(tlen == i) {
    strcpy(arg_start, title);
  } else if(tlen < i) {
    strcpy(arg_start, title);
    memset(arg_start + tlen, 0, i - tlen);
    // 1、当要更改的进程名称串比原始进程名称串短时，填充argv[0]字段时，改为填充argv[0]区的后段，前段填充0
    // memset(arg_start,0,i);
    // strcpy(arg_start + (i - tlen),title);
    //                          
  } else {
    *(char *)mempcpy(arg_start, title, i-1) = '\0';
  }

  if(env_start) {
    p = strchr(arg_start, ' ');
    if(p) *p = '\0';
  }
}


void CloseParentFd(int fd){
   struct rlimit    rl;

   if(getrlimit(RLIMIT_NOFILE, &rl) < 0)
   {
      perror("getrlimit(RLIMIT_NOFILE, &rl)");
      return ;
   }

   if(rl.rlim_max == RLIM_INFINITY)
   {
      rl.rlim_max = 1024;
   }

   for(int i = 0; i < rl.rlim_max; i++)
   {
       if(fd != i){
           close(i);
       }
   }

   return;
}

    void SavePid_X(const char* name)
    {
        pid_t  pid = getpid();

        std::ofstream pid_file;
        pid_file.open(name);
        if(pid_file.is_open()){
            pid_file << pid;
            pid_file.close();
        }else{
            LOG(ERROR) << "agent " << name << "  pid file open error";
        }
    }

    int32_t GetPidByNameFromFile(const char* name) {

        char file_name[64] = {0};

        snprintf(file_name, 64, "%s.pid", name);
        std::string pid_str;
        std::ifstream in(file_name);

        if (in.is_open()) {
            in >> pid_str;
            in.close();
        } else {
            LOG(ERROR) << "agent " << name << "  pid file open error";
            return -1;
        }

        boost::trim(pid_str);

        if (pid_str.empty()) {
            LOG(ERROR) << "agent " << name << "  pid file empty";
            return -1;
        }

        return atoi(pid_str.c_str());
    }


void GetPidByNameAndKill(const char* name){

   char cmdline[1024] = {0};

    int32_t result = GetPidByNameFromFile(name);
    if(result <= 0){
        LOG(ERROR) << "agent " << name << "  pid "<< result;
        return ;
    }

    pid_t agent_pid = static_cast<pid_t>(result);
    snprintf(cmdline, 1024, "cat  /proc/%d/cmdline", agent_pid);

    FILE *fp = popen(cmdline, "r");
    char buffer[64] = {0};
    while (NULL != fgets(buffer, 10, fp))
    {

        if(strcmp(buffer, name) == 0){
           int ret = kill( agent_pid, SIGKILL);
           if (0 != ret) {
            LOG(ERROR) << "Kill process failed!";
            }else{
            LOG(INFO) << "Kill process success!";
            }
        }else{
           LOG(INFO) << "agent not equal!";
        }

        break;
    }
    pclose(fp);
    return ;
}

    char GetCentOSVersion(){
        char cmdline[1024] = {0};
        snprintf(cmdline, 1024, "cat /etc/redhat-release");

        FILE *fp = popen(cmdline, "r");
        char buffer[1024] = {0};
        while (NULL != fgets(buffer, 1024, fp))
        {
            std::string buf(buffer);
            int size = buf.size();
            for(int i = 0; i < size; i++){
                if( '6' <= buf[i] && buf[i] <= '8'){
                    return buf[i];
                }
            }
        }

       return '6';
    }


    void getErrorForSystemStatus(pid_t status){

        if (-1 == status)
        {
            LOG(ERROR) << "system error!";
        }
        else
        {
            LOG(INFO) << "exit status value = !" << status;

            if (WIFEXITED(status))
            {
                if (0 == WEXITSTATUS(status))
                {
                    LOG(INFO) << "run shell script successfully." ;
                }
                else
                {
                    LOG(ERROR) << "run shell script fail, script exit code : " <<   WEXITSTATUS(status);
                }
            }
            else
            {
                LOG(ERROR) << "exit status : " << WEXITSTATUS(status);
            }
        }
    }


} // namespace cplugin 
