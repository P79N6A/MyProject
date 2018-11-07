// =====================================================================================
//
//       Filename:  config_client.h
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-04-17
//       Revision:  none
//
//
// =====================================================================================

#ifndef __getconfig_client__H__
#define __getconfig_client__H__

#include <string>
#include <vector>
#include <map>
#include <pthread.h>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Mutex.h>

#include "sgagent_service_types.h"
#include "comm/sgagent_mq.h"
#include "comm/buffer_mgr.h"
#include "util/msgparam.h"
#include "util/sg_agent_def.h"
#include "comm/auto_lock.h"
#include "task_context.h"
#include "sgcommon/common_interface.h"
#include "mtconfig_server_client.h"
#include "file_config_client.h"

namespace sg_agent {

class ConfigClient {
 public:
  typedef boost::shared_ptr<TaskContext<proc_conf_param_t, proc_conf_param_t> > ConfigContextPtr;
  typedef boost::shared_ptr<proc_conf_param_t> ConfigParamPtr;

  class UpdateConfigHandler : public muduo::net::Task {
   public:
    UpdateConfigHandler(ConfigClient *client) : client_(client) {}
    virtual void Run() {
      client_->UpdateConfigTimer();
    }
   private:
    ConfigClient *client_;
  };

  class SaveConfigToDiskHandler : public muduo::net::Task {
   public:
    SaveConfigToDiskHandler(ConfigClient *client) : client_(client) {}
    virtual void Run() {
      client_->SaveConfigToDiskTimer();
    }
   private:
    ConfigClient *client_;
  };

  class ClearConfigHandler : public muduo::net::Task {
   public:
    ClearConfigHandler(ConfigClient *client) : client_(client) {}
    virtual void Run() {
      client_->ClearConfigTimer();
    }
   private:
    ConfigClient *client_;
  };

  class SyncConfigHandler : public muduo::net::Task {
   public:
    SyncConfigHandler(ConfigClient *client) : client_(client) {}
    virtual void Run() {
      client_->SyncConfigTimer();
    }
   private:
    ConfigClient *client_;
  };

  class MCCHandler : public muduo::net::Task {
   public:
    MCCHandler(ConfigClient *client, ConfigContextPtr context)
        : client_(client), context_(context) {}
    virtual void Run() {
      client_->BackendMCCHandler(context_);
    }
   private:
    ConfigContextPtr context_;
    ConfigClient *client_;
  };


  static ConfigClient *getInstance();
  static void Destroy();

  int Init(int timeout = DEFAULT_CONFIG_TIMEOUT, int retry = DEFAULT_CONFIG_RETRY);

  // 获取本地配置,主要octo内容使用，为了获取octo.conf，用于获取traceId是否打开的配置
  int GetConfig(std::string &_return, const proc_conf_param_t &node);

  int GetConfigFromWorker(std::string &_return, const proc_conf_param_t &node);

  // 设置server端config
  int SetConfig(const proc_conf_param_t &node);

  int GetConfigBeforeSet(const proc_conf_param_t &node);

  // 从notify获得更新的通知， 并请求server端
  int UpdateConfig(const ConfigUpdateRequest &request);

  int GetConfigFromServer(proc_conf_param_t *);
  int SetConfigToServer(proc_conf_param_t *);
  int SyncConfigToServer(proc_conf_param_t *);

  //worker thread handle mtconfig
  void BackendMCCHandler(ConfigContextPtr context);


  // 触发获取更新
  void UpdateConfigTimer();
  void SaveConfigToDiskTimer();
  void ClearConfigTimer();
  void SyncConfigTimer();

 private:

  ConfigClient();
  ~ConfigClient();

  int SendMCCReq(ConfigContextPtr context);

  //generate the dynamic cfg cache key.
  std::string GenCacheKey(const proc_conf_param_t &ndoe);

  // generate the file name while save the cache into disk.
  std::string GenFileName(const proc_conf_param_t &node);

  std::string GenDiskPath(const proc_conf_param_t &node);

  // 将ConfigNode转换为proc_conf_param_t
  int _convertNode(const ConfigNode &node, proc_conf_param_t &param);

  // 将proc_conf_param_t转换为ConfigNode
  int _convertToNode(const proc_conf_param_t &param, ConfigNode &node);

  int _PrintConfigListToLog();

  int _SaveToDisk();

  // 判断从文件读取的一行是否是title， 如果是则返回0，
  // 并且将appkey值复制给引用
  int _isNewApp(std::string &appkey,
                const std::string &content);

  int _LoadFromDisk(proc_conf_param_t &_return,
                    const proc_conf_param_t &ndoe);

  int _genConfParam(proc_conf_param_t &_return,
                    std::string key, std::string value);

  // 更新conf最近访问时间
  int _updateAccessTime(std::string key);

  // 生成getConfig返回json
  int GenRetJson(std::string &_return,
                 const proc_conf_param_t &, const int err);
  int GenRetJson(std::string &_return, const int err,
                 const std::string &conf = "",
                 const int64_t version = 0);

  int m_timeout;          // timeout
  int m_retry;            // retry time


  std::string mConfigPath;
  std::string mConfigFile;

  std::string sgagent_ip;   // sg_agent本机ip

  MtConfigCollector *config_collector;

  // 用于存储分组后的serviceList
  static BufferMgr<proc_conf_param_t> *mConfigBufferMgr;

  /**
   * 用于记录config的所有key信息
   */
  boost::shared_ptr<std::map<std::string, timeval> > keyMap_;

  static ConfigClient *mConfigClient;

  muduo::net::IEventLoopThreadProxy *loop_thread_;

  muduo::net::IEventLoopProxy *loop_;

  muduo::MutexLock keyMapMutexLock_;
  muduo::MutexLock rdDiskMutexLock_;

  static muduo::MutexLock s_cmutex;

};
} //namespace


#endif

