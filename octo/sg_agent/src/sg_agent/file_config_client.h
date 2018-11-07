// =====================================================================================
//
//       Filename:  file_config_client.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-04-17
//       Revision:  none
//
//
// =====================================================================================

#ifndef FILE_CONFIG_CLIENT_H_
#define FILE_CONFIG_CLIENT_H_

#include <string>
#include <vector>
#include <map>
#include "sgagent_service_types.h"
#include "comm/buffer_mgr.h"
#include "util/sg_agent_def.h"
#include "task_context.h"
#include "sgcommon/common_interface.h"
#include <pthread.h>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include "mtconfig_server_client.h"
#include "../util/SGAgentErr.h"
#include <muduo/base/Mutex.h>

namespace sg_agent {

class FileConfigClient {
 public:
  typedef boost::shared_ptr<TaskContext<file_param_t, file_param_t> > FileConfigContextPtr;

  class FileConfigHandler : public muduo::net::Task {
   public:
    FileConfigHandler(FileConfigClient *client, FileConfigContextPtr context)
        : client_(client), context_(context) {}
    virtual void Run() {
      client_->FileConfigBackendHandler(context_);
    }
   private:
    FileConfigContextPtr context_;
    FileConfigClient *client_;
  };


  FileConfigClient();
  ~FileConfigClient();

  static FileConfigClient *getInstance();
  static void Destroy();

  int init(int timeout = DEFAULT_FILECONFIG_TIMEOUT, int retry = DEFAULT_CONFIG_RETRY);

  int getFileConfig(file_param_t &returnFile, const file_param_t &node);

  int getFileConfigFromWorker(file_param_t &node);

  // 从notify获得下发文件的通知
  int notifyIssued(const file_param_t &notice);

  // 获取update的结果
  int notifyWork(const file_param_t &command);

  void FileConfigBackendHandler(FileConfigContextPtr context);
  // 定时获取文件配置可用指标
  void FileConfigAvailQuotaTimer();


  //周期同步配置文件
  void SyncConfigPeriodicTimer();


 private:


  std::string _genKey(const file_param_t &node);

  //加载配置文件到缓存中，并计算md5值
  int _loadFileToBuffer(const std::string &key, ConfigFile &file);

  int BackendErrorToFrontError(int error);

  //将 ConfigFile 转换为FileConfigSyncRequest
  int ConvertToSyncNode(const ConfigFile &param, FileConfigSyncRequest &node, const std::string &appKey);
  int GetAppkeyFromKey(std::string &key, std::string &appKey);

  int m_timeout;          // timeout
  int m_retry;            // retry time



  std::string m_basepath;

  MtConfigCollector *config_collector;
  muduo::net::IEventLoopThreadProxy *loop_thread_;

  muduo::net::IEventLoopProxy *loop_;

  static BufferMgr<ConfigFile> *mFileConfigBufferMgr;

  static FileConfigClient *mFileConfigClient;
  static pthread_mutex_t m_fMutex;
};
} // namespace sg_agent

#endif // FILE_CONFIG_CLIENT_H_

