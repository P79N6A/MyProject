#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <string>
#include <iostream>
#include <glog/logging.h>

#include "host_manager.h"
#include "core_server.h"
#include "util/process.h"

using namespace std;
using namespace cplugin;

int g_argc;
char** g_argv;

static const char* kLogDir = "/var/sankuai/logs/cplugin";
static HostManager* g_host_manager = NULL;

static muduo::net::EventLoopThread g_server_thread;
static muduo::net::EventLoop* g_server_loop_ = NULL;

static void SigChldHandler(int signo) {

  if(!g_host_manager)  {
      return;
  }

  if (g_host_manager->is_stopping()) {
    LOG(INFO) << "Host manager is stopping process, do nothing here.";
    return;
  }

  pid_t pid = -1;

  pid = waitpid(-1, NULL, WNOHANG);
  if (0 == pid) {
    LOG(INFO) << "No process need to be killed.";
  } else if (pid > 0) {
    LOG(INFO) << "Process " << pid << " has been killed.";
    g_host_manager->ResetPlugin(pid);
  } else {
    LOG(ERROR) << "Waitpid() error, errno is " << errno;
  }
}

static void SigTermHandler(int signo) {
  if(g_host_manager)  {
      HostManager* local_manager = g_host_manager;
      g_host_manager = NULL;

      local_manager->StopAll();

      sleep(2);
      delete local_manager;
      google::ShutdownGoogleLogging();
      _exit(0);
  }
}

static void SignalRoutine(int signo) {
  switch (signo) {
    case SIGCHLD:
      SigChldHandler(signo);
      break;
    case SIGTERM:
      SigTermHandler(signo);
      break;
    default:
      break;
  }
}

static void StartServer(){
    CoreServer server(g_host_manager);
    server.Start();
}

int main(int argc, char* argv[]) {
  // do some pre job 
  getErrorForSystemStatus(system("sh /service/cplugin/pre.sh"));

  if (access(kLogDir, NULL) != 0) {
    if (mkdir(kLogDir, 0755) < 0) {
      perror("mkdir error.");
    }
  }

  g_argc = argc;
  g_argv = argv;

  google::InitGoogleLogging(argv[0]);
  google::SetLogDestination(google::GLOG_INFO, 
                            "/var/sankuai/logs/cplugin/cplugin.log.");
  FLAGS_logbufsecs = 0;
  FLAGS_max_log_size = 64;
  FLAGS_stop_logging_if_full_disk = true;
  //google::InstallFailureSignalHandler();

  // Set signal
  // signal(SIGCHLD, SignalRoutine);
  signal(SIGTERM, SignalRoutine);

  LOG(INFO) << "Plugin platform start.";
  g_host_manager = new HostManager;

  g_server_loop_ = g_server_thread.startLoop();

  g_server_loop_->runInLoop(boost::bind(StartServer));

  if(!g_host_manager->Init()) {
        LOG(ERROR) << "HostManager init failed.";
        sleep(5);
        return -1;
  }

  SavePid_X("cplugin.pid");

  do{
      sleep(10);
  }while(1);


  g_host_manager->StopAll();
  delete g_host_manager;
  google::ShutdownGoogleLogging();
  return 0;
}
